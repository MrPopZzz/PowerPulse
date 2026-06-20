package com.energytracker.usage_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.energytracker.usage_service.dto.UsageDTO;
import com.energytracker.usage_service.service.UsageService;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

	private final UsageService usageService;
	
	public UsageController(UsageService usageService) {
		this.usageService = usageService;
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UsageDTO> getUserDeviceUsage(@PathVariable Long userId, 
			@RequestParam(defaultValue = "3") int days) {
		
		final UsageDTO usage = usageService.getXDaysUsageForUser(userId, days);
		return ResponseEntity.ok(usage);
	}
}
