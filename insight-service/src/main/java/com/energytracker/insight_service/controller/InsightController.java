package com.energytracker.insight_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energytracker.insight_service.dto.InsightDTO;
import com.energytracker.insight_service.service.InsightService;

@RestController
@RequestMapping("/api/v1/insight")
public class InsightController {

	private final InsightService insightService;
	
	public InsightController(InsightService insightService) {
		this.insightService = insightService;
	}
	
	@GetMapping("/saving-tips/{userId}")
	public ResponseEntity<InsightDTO> getSavingTips(@PathVariable Long userId) {
		final InsightDTO insight = insightService.getSavingsTips(userId);
		return ResponseEntity.ok(insight);
	}
	
	@GetMapping("/overview/{userId}")
	public ResponseEntity<InsightDTO> getOverview(@PathVariable Long userId) {
		final InsightDTO insight = insightService.getOverview(userId);
		return ResponseEntity.ok(insight);
	}
}
