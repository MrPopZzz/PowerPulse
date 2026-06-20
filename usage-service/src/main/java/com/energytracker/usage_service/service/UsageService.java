package com.energytracker.usage_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.energytracker.kafka.event.AlertingEvent;
import com.energytracker.kafka.event.EnergyUsageEvent;
import com.energytracker.usage_service.client.DeviceClient;
import com.energytracker.usage_service.client.UserClient;
import com.energytracker.usage_service.dto.DeviceDTO;
import com.energytracker.usage_service.dto.UsageDTO;
import com.energytracker.usage_service.dto.UserDTO;
import com.energytracker.usage_service.model.Device;
import com.energytracker.usage_service.model.DeviceEnergy;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsageService {
	
	private InfluxDBClient influxDBClient;
	private DeviceClient deviceClient;
	private UserClient userClient;
	
	@Value("${influx.bucket}")
	private String influxBucket;
	
	@Value("${influx.org}")
	private String influxOrg;
	
	private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;
	
	public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, 
			UserClient userClient, KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
		
		this.influxDBClient = influxDBClient;
		this.deviceClient = deviceClient;
		this.userClient = userClient;
		this.kafkaTemplate = kafkaTemplate;
	}

	@KafkaListener(topics = "energy-uasge", groupId = "usage-service")
	public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
		log.info("Recieved energy usage event: {}", energyUsageEvent);
		
		// Process the energy usage event and store it in influxDB
		Point point = Point.measurement("energy_usage")
				.addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
				.addField("energyConsumed", energyUsageEvent.energyConsumed())
				.time(energyUsageEvent.timestamp(), WritePrecision.MS);
		
		influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
	}
	
	@Scheduled(cron = "*/10 * * * * *")
	public void aggregateDeviceEnergyUsage() {
		final Instant now = Instant.now();
		final Instant oneHourAgo = now.minusSeconds(3600);
		
		// InfluxDB Query
		String fluxQuery = String.format("""
				from(bucket: "%s")
					|> range(start: time(v: "%s"), stop: time(v: "%s"))
					|> filter(fn: (r) => r["_measurement"] == "energy_usage")
					|> filter(fn: (r) => r["_field"] == "energyConsumed")
					|> group(columns: ["deviceId"])
					|> sum(column: "_value")
				""", influxBucket, oneHourAgo.toString(), now);
		
		// Execute the query and process results...
		QueryApi queryApi = influxDBClient.getQueryApi();
		List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);
		
		List<DeviceEnergy> deviceEnergies = new ArrayList<>();
		for(FluxTable table : tables) {
			for(FluxRecord record : table.getRecords()) {
				String deviceIdStr = (String) record.getValueByKey("deviceId");
				Double energyConsumed = record.getValueByKey("_value") instanceof Number ?
						((Number) record.getValueByKey("_value")).doubleValue() : 0.0;
				
				// Add to deviceEnergies
				deviceEnergies.add(
						DeviceEnergy.builder()
							.deviceId(Long.valueOf(deviceIdStr))
							.energyConsumed(energyConsumed)
							.build()
				);
			}
		}
		log.info("Aggregated device energies over the past hour: {}", deviceEnergies);
		
		// Get data from device-service
		for(DeviceEnergy deviceEnergy : deviceEnergies) {
			try {
				final DeviceDTO deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
				
				if (deviceResponse == null) {
					log.warn("Device not found for ID: {}", deviceEnergy.getDeviceId());
					continue;
				}
				
				deviceEnergy.setUserId(deviceResponse.userId());
			} catch (Exception e) {
				log.warn("Failed to fetch device for ID: {}", deviceEnergy.getDeviceId());
			}
		}
		
		// Further processing - remove all those devices for which we find no userId
		deviceEnergies.removeIf(dE -> dE.getUserId() == null);
		
		// Get user-service mapping and aggregate per user
		Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream()
				.collect(Collectors.groupingBy(DeviceEnergy::getUserId));
		log.info("User-Device Energy Map: {}", userDeviceEnergyMap);
		
		// Get energy consumption per user threshold
		List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
		final Map<Long, Double> userThresholdMap = new HashMap<>();
		final Map<Long, String> userEmailMap = new HashMap<>();
		
		for(final Long userId : userIds) {
			try {
				UserDTO user = userClient.getUserById(userId);
				
				if(user == null || user.id() == null || !user.alerting()) {
					log.warn("User not found or alerting disabled for ID: {}", userId);
					continue;
				}
				
				userThresholdMap.put(userId, user.energyAlertingThreshold());
				userEmailMap.put(userId, user.email());
			} catch (Exception e) {
				log.warn("Failed to fetch user for ID: {}", userId);
			}
		}
		log.info("User Threshold Map: {}", userThresholdMap);
		
		// Check thresholds against aggregated energy usage
		final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
		
		for(final Long userId : alertedUsers) {
			final Double threshold = userThresholdMap.get(userId);
			final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);
			
			final Double totalEnergyConsumption = devices.stream()
					.mapToDouble(DeviceEnergy::getEnergyConsumed)
					.sum();
			
			if (totalEnergyConsumption > threshold) {
				log.info("ALERT: User ID {} has exceeded the energy threshold! Total Energy Consumption: {}, Threshold: {}", 
						userId, totalEnergyConsumption, threshold);
				
				// Put message on kafka alert-topic
				final AlertingEvent alertingEvent = AlertingEvent.builder()
						.userId(userId)
						.message("Energy consumption threshold exceeded")
						.threshold(threshold)
						.energyConsumed(totalEnergyConsumption)
						.email(userEmailMap.get(userId))
						.build();
				
				// send
				kafkaTemplate.send("energy-alerts", alertingEvent);
			} else {
				log.info("User ID {} is within the energy threshold. " + 
						"Total Consumption: {}, Threshold: {}", 
						userId, totalEnergyConsumption, threshold);
			}
		}
	}
	
	public UsageDTO getXDaysUsageForUser(Long userId, int days) {
		log.info("Getting usage for userId: {} over past {} days", userId, days);
		
		final List<DeviceDTO> devicesDTO = deviceClient.getAllDevicesForUser(userId);
		
		final List<Device> devices = new ArrayList<>();
		for(DeviceDTO deviceDTO : devicesDTO) {
			devices.add(Device.builder()
					.id(deviceDTO.id())
					.name(deviceDTO.name())
					.type(deviceDTO.type())
					.location(deviceDTO.location())
					.userId(deviceDTO.userId())
					.build());
		}
		
		if(devices == null || devices.isEmpty()) {
			return UsageDTO.builder()
					.userId(userId)
					.devices(null)
					.build();
		}
		
		// build a set of device IDs to filter on flux query
		List<String> deviceIdStrings = devices.stream()
				.map(Device::getId)
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.toList();
		
		final Instant now = Instant.now();
		final Instant start = now.minusSeconds((long) days * 24 * 3600);
		
		// build device filter "r[\"deviceId\"] == \"1\" or r[\"deviceId\"] == \"2\""
		final String deviceFilter = deviceIdStrings.stream()
				.map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
				.collect(Collectors.joining(" or "));
		
		String fluxQuery = String.format("""
				from(bucket: "%s")
					|> range(start: time(v: "%s"), stop: time(v: "%s"))
					|> filter(fn: (r) => r["_measurement"] == "energy_usage")
					|> filter(fn: (r) => r["_field"] == "energyConsumed")
					|> filter(fn: (r) => %s)
					|> group(colums: ["deviceId"])
					|> sum(column: "_value")
				""", influxBucket, start.toString(), now.toString(), deviceFilter);
		
		final Map<Long, Double> aggregateMap = new HashMap<>();
		
		try {
			QueryApi queryApi = influxDBClient.getQueryApi();
			List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);
			
			for(FluxTable table : tables) {
				for(FluxRecord record : table.getRecords()) {
					Object deviceIdObj = record.getValueByKey("deviceId");
					String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
					
					if(deviceIdStr == null) {
						continue;
					}
					
					Double energyConsumed = record.getValueByKey("_value") instanceof Number 
							? ((Number) record.getValueByKey("_value")).doubleValue() 
							: 0.0;
					
					try {
						Long deviceId = Long.valueOf(deviceIdStr);
						aggregateMap.put(deviceId, aggregateMap.getOrDefault(deviceId, 0.0) + energyConsumed);
					} catch (NumberFormatException nfe) {
						log.warn("Failed to parse deviceId from influx record: {}", deviceIdStr);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage());
			
			// set aggregatedConsumption to 0.0 on error
			devices.forEach(d -> d.setEnergyConsumed(0.0));
			return UsageDTO.builder()
					.userId(userId)
					.devices(null)
					.build();
		}
		
		// populate aggregated energy consumed per device
		for(Device device : devices) {
			if(device == null || device.getId() == null) {
				continue;
			}
			
			device.setEnergyConsumed(aggregateMap.getOrDefault(device.getId(), 0.0));
		}
		
		log.info("Aggregated energy consumption for userId {}: {}", userId, aggregateMap);
		
		final List<DeviceDTO> resultDevices = devices.stream()
				.map(d -> DeviceDTO.builder()
						.id(d.getId())
						.name(d.getName())
						.type(d.getType())
						.location(d.getLocation())
						.userId(d.getUserId())
						.energyConsumed(d.getEnergyConsumed())
						.build())
				.toList();
		
		return UsageDTO.builder()
				.userId(userId)
				.devices(resultDevices)
				.build();
	}
}
