package com.energytracker.user.service;

import org.springframework.stereotype.Service;

import com.energytracker.user.dto.UserDTO;
import com.energytracker.user.entity.User;
import com.energytracker.user.exception.UserNotFoundException;
import com.energytracker.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserDTO createUser(UserDTO input) {
//		log.info("Creating user: {}", input);
		
		final User createdUser = User.builder()
				.firstName(input.getFirstName())
				.lastName(input.getLastName())
				.email(input.getEmail())
				.alerting(input.isAlerting())
				.energyAlertingThreshold(input.getEnergyAlteringThreshold())
				.build();
		
		final User saved = userRepository.save(createdUser);
		
		return toDTO(saved); 
	}
	
	public UserDTO getUserById(Long id) {
//		log.info("Getting user by id: {}", id);
		
		return userRepository.findById(id)
				.map(this::toDTO)
				.orElse(null);
	}
	
	public void updateUser(Long id, UserDTO userDTO) {
//		log.info("Updating user with id: {}", id);
		
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setEmail(userDTO.getEmail());
		user.setAddress(userDTO.getAddress());
		user.setAlerting(userDTO.isAlerting());
		user.setEnergyAlertingThreshold(userDTO.getEnergyAlteringThreshold());
		
		userRepository.save(user);
	}
	
	public void deleteUser(Long id) {
//		log.info("Deleting user with id: {}", id);
		
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		
		userRepository.delete(user);
	}
	
	// Helper methods
	private UserDTO toDTO(User user) {
		return UserDTO.builder()
				.id(user.getId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.address(user.getAddress())
				.alerting(user.isAlerting())
				.energyAlteringThreshold(user.getEnergyAlertingThreshold())
				.build();
	}
}
