package com.energytracker.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String address;
	private boolean alerting;
	private double energyAlteringThreshold;
}
