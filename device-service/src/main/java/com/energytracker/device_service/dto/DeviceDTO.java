package com.energytracker.device_service.dto;

import com.energytracker.device_service.model.DeviceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeviceDTO {

	private Long id;
	private String name;
	private DeviceType type;
	private String location;
	private Long userId;
}
