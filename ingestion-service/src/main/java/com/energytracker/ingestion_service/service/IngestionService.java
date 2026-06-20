package com.energytracker.ingestion_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.energytracker.ingestion_service.dto.EnergyUsageDTO;
import com.energytracker.kafka.event.EnergyUsageEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IngestionService {

	private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;
	
	public IngestionService(KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void ingestEnergyUsage(EnergyUsageDTO input) {
		// Convert DTO to event
		EnergyUsageEvent event = EnergyUsageEvent.builder()
				.deviceId(input.deviceId())
				.energyConsumed(input.energyConsumed())
				.timestamp(input.timestamp())
				.build();
		
		// Send to kafka topic
		kafkaTemplate.send("energy-usage", event);
		log.info("Ingested Energy Usage Event: {}", event);
	}
}
