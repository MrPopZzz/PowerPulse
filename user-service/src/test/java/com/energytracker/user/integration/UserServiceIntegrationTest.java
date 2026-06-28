package com.energytracker.user.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.energytracker.user.dto.UserDTO;
import com.energytracker.user.repository.UserRepository;
import com.energytracker.user.testsupport.MySqlTestcontainersBase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends MySqlTestcontainersBase {

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	void createUser_viaRestApi_persistsAndReturnsUser() {
		UserDTO request = UserDTO.builder()
				.firstName("Sayan")
				.lastName("Chakraborty")
				.email("sayanc2@gmail.com")
				.address("123 x Lane")
				.alerting(true)
				.energyAlteringThreshold(2500.0)
				.build();
		
		ResponseEntity<UserDTO> response = restTemplate.postForEntity("/api/v1/user", request, UserDTO.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
		assertThat(response.getBody().getFirstName()).isEqualTo("Sayan");
		assertThat(response.getBody().getLastName()).isEqualTo("Chakraborty");
		assertThat(response.getBody().getAddress()).isEqualTo("123 x Lane");
		assertThat(response.getBody().getEmail()).isEqualTo("sayanc2@gmail.com");
		assertThat(response.getBody().isAlerting()).isTrue();
		assertThat(response.getBody().getEnergyAlteringThreshold()).isEqualTo(2500.0);
		
		ResponseEntity<UserDTO> loaded = restTemplate.getForEntity("/api/v1/user" + response.getBody().getId(),
				UserDTO.class);
		
		assertThat(loaded.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(loaded.getBody()).isNotNull();
		assertThat(loaded.getBody().getEmail()).isEqualTo("sayanc2@gmail.com");
	}
}
