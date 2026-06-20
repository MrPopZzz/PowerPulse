package com.energytracker.usage_service.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.energytracker.usage_service.dto.DeviceDTO;

@Component
public class DeviceClient {

	private final RestTemplate restTemplate;
	private String baseURL;
	
	public DeviceClient(RestTemplate restTemplate, @Value("${device.service.url}") String baseURL) {
		this.restTemplate = restTemplate;
		this.baseURL = baseURL;
	}
	
	public DeviceDTO getDeviceById(Long deviceId) {
		String url = UriComponentsBuilder
				.fromUriString(baseURL)
				.path("/{deviceId}")
				.buildAndExpand(deviceId)
				.toUriString();
		
		ResponseEntity<DeviceDTO> response = restTemplate.getForEntity(url, DeviceDTO.class);
		return response.getBody();
	}
	
	public List<DeviceDTO> getAllDevicesForUser(Long userId) {
		String url = UriComponentsBuilder
				.fromUriString(baseURL)
				.path("/user/{userId}")
				.buildAndExpand(userId)
				.toUriString();
		
		ResponseEntity<DeviceDTO> response = restTemplate.getForEntity(url, DeviceDTO.class);
		DeviceDTO devcies = response.getBody();
		return devcies == null ? List.of() : List.of(devcies);
	}
}
