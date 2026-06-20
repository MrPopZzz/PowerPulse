package com.energytracker.ingestion_service.simulation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.energytracker.ingestion_service.dto.EnergyUsageDTO;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ParallelDataSimulator implements CommandLineRunner {
	
	@Value("${simulation.parallel-threads}")
	private int parallelThreads;
	
	@Value("${simulation.requests-per-interval}")
	private int requestsPerInterval;
	
	@Value("${simulation.endpoint}")
	private String ingestionEndpoint;
	
	private final ExecutorService executorService;
	private final Random random = new Random();
	private final RestTemplate restTemplate = new RestTemplate();
	
	public ParallelDataSimulator() {
		this.executorService = Executors.newCachedThreadPool();
	}
	
	@Override
	public void run(String... args) throws Exception {
		log.info("ParallelDataSimulator started...");
		
		((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
	}
	
	@Scheduled(fixedRateString = "${simulation.interval-ms}")
	public void sendMockData() {
		int batchSize = requestsPerInterval / parallelThreads;
		int remainder = requestsPerInterval % parallelThreads;
		
		for(int i = 0; i < parallelThreads; i++) {
			int requestsForThread = batchSize + (i < remainder ? 1 : 0);
			
			executorService.submit(() -> {
				for(int j = 0; j < requestsForThread; j++) {
					EnergyUsageDTO dto = EnergyUsageDTO.builder()
							.deviceId(random.nextLong(1, 200))
							.energyConsumed(Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0)
							.timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
							.build();
					
					try {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						HttpEntity<EnergyUsageDTO> request = new HttpEntity<>(dto, headers);
						restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
						
						log.info("Sent mock data: {}", dto);
					} catch (Exception e) {
						log.error("Failed to send data: {}", e.getMessage());
					}
				}
			});
		}
	}
	
	@PreDestroy
	public void shutdown() {
		executorService.shutdown();
		log.info("ParallelDataSimulator shutting down...");
	}

}
