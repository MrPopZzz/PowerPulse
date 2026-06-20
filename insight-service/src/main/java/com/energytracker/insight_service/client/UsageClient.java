package com.energytracker.insight_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.energytracker.insight_service.dto.UsageDTO;

@Component
public class UsageClient {

	private final RestTemplate restTemplate;
	private final String baseURL;
	
	public UsageClient(RestTemplate restTemplate, @Value("${usage.service.url}") String baseURL) {
		this.restTemplate = restTemplate;
		this.baseURL = baseURL;
	}
	
	public UsageDTO getXDaysUsageForUser(Long userId, int days) {
		String url = UriComponentsBuilder
				.fromUriString(baseURL)
				.path("/{userId}")
				.queryParam("days", days)
				.buildAndExpand(userId)
				.toUriString();
		
		ResponseEntity<UsageDTO> response = restTemplate.getForEntity(url, UsageDTO.class);
		return response.getBody();
	}
}
