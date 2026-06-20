package com.energytracker.usage_service.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record UsageDTO(
	Long userId,
	List<DeviceDTO> devices
) {}
