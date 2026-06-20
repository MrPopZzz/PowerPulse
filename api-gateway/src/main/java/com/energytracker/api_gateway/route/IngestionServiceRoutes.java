package com.energytracker.api_gateway.route;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.net.URI;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class IngestionServiceRoutes {
	
	@Bean
	public RouterFunction<ServerResponse> ingestionRoutes() {
		return GatewayRouterFunctions.route("ingestion-service")
				.route(RequestPredicates.path("/api/v1/ingestion/**"), http())
				.before(uri("http://localhost:8082"))
				.filter(CircuitBreakerFilterFunctions.circuitBreaker(
						"ingestionServiceCircuitBreaker", 
						URI.create("forward:/fallbackRoute")
				))
				.build();
	}
	
	@Bean
	public RouterFunction<ServerResponse> ingestionFallbackRoute() {
		return GatewayRouterFunctions.route("fallbackRoute")
				.route(RequestPredicates.path("/fallbackRoute"), 
						request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Ingestion service is down"))
		.build();		
	}
}
