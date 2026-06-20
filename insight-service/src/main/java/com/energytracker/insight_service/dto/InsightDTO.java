package com.energytracker.insight_service.dto;

import lombok.Builder;

@Builder
public record InsightDTO(
	Long userId,
	String tips,
	double energyUsage
) {}
