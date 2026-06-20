package com.energytracker.insight_service.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import com.energytracker.insight_service.client.UsageClient;
import com.energytracker.insight_service.dto.DeviceDTO;
import com.energytracker.insight_service.dto.InsightDTO;
import com.energytracker.insight_service.dto.UsageDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InsightService {

	private final UsageClient usageClient;
	private OllamaChatModel ollamaChatModel;
	
	public InsightService(UsageClient usageClient, OllamaChatModel ollamaChatModel) {
		this.usageClient = usageClient;
		this.ollamaChatModel = ollamaChatModel;
	}
	
	public InsightDTO getSavingsTips(Long userId) {
		// Fetch data from usage service
		final UsageDTO usageData = usageClient.getXDaysUsageForUser(userId, 3);
		
		
		double totalUsage = usageData.devices()
				.stream()
				.mapToDouble(DeviceDTO::energyConsumed)
				.sum();
		
		log.info("Calling Ollama for userId {} with total usage {}", userId, totalUsage);
		
		String prompt = new StringBuilder()
				.append("This is the total consumption over the past 3 days.")
				.append("How can I reduce my energy consumption? How does it compare to average households?")
				.append("Total energy used: \n")
				.append(totalUsage)
				.toString();
		
		ChatResponse response = ollamaChatModel.call(
				Prompt.builder()
					.content(prompt)
					.build()
		);
		
		return InsightDTO.builder()
				.userId(userId)
				.tips(response.getResult().getOutput().getText())
				.energyUsage(totalUsage)
				.build();
	}
	
	public InsightDTO getOverview(Long userId) {
		// Fetch data from usage service
		final UsageDTO usageData = usageClient.getXDaysUsageForUser(userId, 3);
		
		
		double totalUsage = usageData.devices()
				.stream()
				.mapToDouble(DeviceDTO::energyConsumed)
				.sum();
		
		log.info("Calling Ollama for userId {} with total usage {}", userId, totalUsage);
		
		String prompt = new StringBuilder()
				.append("Analyze the following energy usage data and provide a concise overview with " +
						"actionable insights.")
				.append("This data is the aggregate data for the past 3 days.")
				.append("Usage data: \n")
				.append(usageData.devices())
				.toString();
		
		ChatResponse response = ollamaChatModel.call(
				Prompt.builder()
					.content(prompt)
					.build()
		);
		
		return InsightDTO.builder()
				.userId(userId)
				.tips(response.getResult().getOutput().getText())
				.energyUsage(totalUsage)
				.build();
	}
}
