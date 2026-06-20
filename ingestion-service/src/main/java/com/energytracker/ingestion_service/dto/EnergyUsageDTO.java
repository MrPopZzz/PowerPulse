package com.energytracker.ingestion_service.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record EnergyUsageDTO (

	Long deviceId,
	double energyConsumed,
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Instant timestamp) {}
