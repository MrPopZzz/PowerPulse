package com.energytracker.ingestion_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.energytracker.ingestion_service.dto.EnergyUsageDTO;
import com.energytracker.ingestion_service.service.IngestionService;

@RestController
@RequestMapping("api/v1/ingestion")
public class IngestionController {

	private final IngestionService ingestionService;
	
	public IngestionController(IngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}
	
	@PostMapping
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	public void ingestData(@RequestBody EnergyUsageDTO usageDTO) {
		ingestionService.ingestEnergyUsage(usageDTO);
	}
}
