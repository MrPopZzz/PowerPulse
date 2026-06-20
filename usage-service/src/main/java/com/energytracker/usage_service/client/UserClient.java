package com.energytracker.usage_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.energytracker.usage_service.dto.UserDTO;

@Component
public class UserClient {

	private final RestTemplate restTemplate;
	private String baseURL;
	
	public UserClient(RestTemplate restTemplate, @Value("${user.service.url}") String baseURL) {
		this.restTemplate = restTemplate;
		this.baseURL = baseURL;
	}
	
	public UserDTO getUserById(Long userId) {
		String url = UriComponentsBuilder
				.fromUriString(baseURL)
				.path("/{userId}")
				.buildAndExpand(userId)
				.toUriString();
		
		ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);
		return response.getBody();
	}
}
