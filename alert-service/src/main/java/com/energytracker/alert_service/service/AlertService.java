package com.energytracker.alert_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.energytracker.kafka.event.AlertingEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlertService {

	private final EmailService emailService;
	
	public AlertService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	@KafkaListener(topics = "energy-alerts", groupId = "alert-service")
	public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
		log.info("Recieved Alert Event: {}", alertingEvent);
		
		// send email alert
		final String subject = "Energy Usage Alert for User " + alertingEvent.getUserId();
		final String message = "Alert: " + alertingEvent.getMessage() + 
					"\nThreshold: " + alertingEvent.getThreshold() +
					"\nEnergy Consumed: " + alertingEvent.getEnergyConsumed();
		
		emailService.sendEmail(alertingEvent.getEmail(), subject, message, alertingEvent.getUserId());
	}
}
