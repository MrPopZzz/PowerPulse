package com.energytracker.insight_service.dto;

import lombok.Builder;

@Builder
public record DeviceDTO(
	Long id,
	String name,
	String type,
	String location,
	double energyConsumed
) {}
