package com.energytracker.insight_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI insightServiceApiDocs() {
		return new OpenAPI()
				.info(new Info()
						.title("Insight Service API")
						.description("Insight service API for PowerPulse")
						.contact(getContact())
						.license(getLicense())
						.version("1.0.0"));
				
	}
	
	private static Contact getContact() {
		Contact contact = new Contact();
		contact.setUrl("https://github.com/MrPopZzz");
		contact.setEmail("chakrabortysayan.36.sc@gmail.com");
		return contact;
	}
	
	private static License getLicense() {
		License license = new License();
		license.setName("Creative Commons Attribution-NonCommercial 4.0 International License");
		license.setUrl("https://creativecommons.org/license/by-nc/4.0/");
		return license;
	}
}
