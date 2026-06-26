package com.energytracker.api_gateway.route;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.setPath;
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
public class DeviceServiceRoutes {

	@Bean
	public RouterFunction<ServerResponse> deviceRoutes() {
		return GatewayRouterFunctions.route("device-service")
				.route(RequestPredicates.path("/api/v1/device/**"), http())
				.before(uri("http://localhost:8081"))
				.filter(CircuitBreakerFilterFunctions.circuitBreaker(
						"deviceServiceCircuitBreaker", 
						URI.create("forward:/fallbackRoute")
				))
				.build();
	}
	
	@Bean
	public RouterFunction<ServerResponse> deviceFallbackRoute() {
		return GatewayRouterFunctions.route("fallbackRoute")
				.route(RequestPredicates.path("/fallbackRoute"), 
						request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Device service is down"))
		.build();		
	}
	
	@Bean
	public RouterFunction<ServerResponse> deviceServiceApiDocs() {
		return GatewayRouterFunctions.route("device-service-api-docs")
				.route(RequestPredicates.path("/docs/device-service/v3/api-docs"), http())
				.before(uri("http://localhost:8081"))
				.before(setPath("/v3/api-docs"))
				.build();
	}
}
