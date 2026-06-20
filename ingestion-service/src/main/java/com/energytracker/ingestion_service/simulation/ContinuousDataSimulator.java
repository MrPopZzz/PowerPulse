package com.energytracker.ingestion_service.simulation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.energytracker.ingestion_service.dto.EnergyUsageDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContinuousDataSimulator implements CommandLineRunner {

	private final RestTemplate restTemplate = new RestTemplate();
	private final Random random = new Random();
	
	@Value("${simulation.requests-per-interval}")
	private int requestsPerInterval;
	
	@Value("${simulation.endpoint}")
	private String ingestionEndpoint;
	
	@Override
	public void run(String... args) throws Exception {
		log.info("ContinuousDataSimulator started...");
	}
	
	//@Scheduled(fixedRateString = "${simulation.interval-ms}")
	public void sendMockData() {
		// Mock data
		for(int i = 0; i < requestsPerInterval; i++) {
			EnergyUsageDTO dto = EnergyUsageDTO.builder()
					.deviceId(random.nextLong(1, 6))
					.energyConsumed(Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0)
					.timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
					.build();
			
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				HttpEntity<EnergyUsageDTO> request = new HttpEntity<EnergyUsageDTO>(dto, headers);
				restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
				
				log.info("Sent mock data: {}", dto);
			} catch (Exception e) {
				log.error("Failed to send data: {}", e.getMessage());
			}
		}
	}
	
	
}
