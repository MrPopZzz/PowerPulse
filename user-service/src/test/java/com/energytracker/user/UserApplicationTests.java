package com.energytracker.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.energytracker.user.entity.User;
import com.energytracker.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class UserApplicationTests {

	public static final int NUMBER_OF_USERS = 10;
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	void contextLoads() {
	}
	
	@Test
	void addUsersToDB() {
		for(int i=1; i <= NUMBER_OF_USERS; i++) {
			User user = User.builder()
					.firstName("User" + i)
					.lastName("Last name" + i)
					.email("user" + i + "@example.com")
					.address(i + " Example WB")
					.alerting(i % 2 == 0)
					.energyAlertingThreshold(1000.0 + i)
					.build();
			userRepository.save(user);
		}
		
		log.info("User repository has been populated");
	}

}
