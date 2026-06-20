package com.energytracker.usage_service.dto;

import lombok.Builder;

@Builder
public record DeviceDTO(
		Long id,
		String name,
		String type,
		String location,
		Long userId,
		double energyConsumed
) {}
