package com.energytracker.usage_service.dto;

public record UserDTO (
	Long id,
	String firstName,
	String lastName,
	String email,
	String address,
	boolean alerting,
	double energyAlertingThreshold
){}
