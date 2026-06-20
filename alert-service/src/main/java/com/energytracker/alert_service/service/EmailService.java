package com.energytracker.alert_service.service;

import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.energytracker.alert_service.entity.Alert;
import com.energytracker.alert_service.repository.AlertRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;
	private final AlertRepository alertRepository;
	
	public EmailService(JavaMailSender mailSender, AlertRepository alertRepository) {
		this.mailSender = mailSender;
		this.alertRepository = alertRepository;
	}
	
	public void sendEmail(String to, String subject, String body, Long userId) {
		log.info("Sending email to: {}, subject: {}", to, subject);
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setFrom("noreply@energytracker.com");
		message.setSubject(subject);
		message.setText(body);
		
		try {
			mailSender.send(message);
			
			// save it on mysql db
			final Alert alertSent = Alert.builder()
					.sent(true)
					.createdAt(LocalDateTime.now())
					.userId(userId)
					.build();
			
			alertRepository.saveAndFlush(alertSent);
		} catch (Exception e) {
			log.error("Failed to send email to: {}", to, e);
			
			// save it on mysql db
			final Alert alertSent = Alert.builder()
					.sent(false)
					.createdAt(LocalDateTime.now())
					.userId(userId)
					.build();
			
			alertRepository.saveAndFlush(alertSent);
			return;
		}
		
		log.info("Email sent to: {}", to);
	}
}
