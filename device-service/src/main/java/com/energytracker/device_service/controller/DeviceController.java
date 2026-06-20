package com.energytracker.device_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energytracker.device_service.dto.DeviceDTO;
import com.energytracker.device_service.service.DeviceService;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

	private DeviceService deviceService;
	
	public DeviceController(DeviceService deviceService) {
		this.deviceService = deviceService;
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id) {
		DeviceDTO device = deviceService.getDeviceById(id);
		return ResponseEntity.ok(device);
	}
	
	@PostMapping("/create")
	public ResponseEntity<DeviceDTO> createDevice(@RequestBody DeviceDTO deviceDTO) {
		DeviceDTO createdDevice = deviceService.createDevice(deviceDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<DeviceDTO> updateDevice(@PathVariable Long id, @RequestBody DeviceDTO deviceDTO) {
		DeviceDTO updatedDevice = deviceService.updateDevice(id, deviceDTO);
		return ResponseEntity.ok(updatedDevice);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
		deviceService.deleteDevice(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<DeviceDTO>> getAllDevicesByUserId(@PathVariable Long userId) {
		List<DeviceDTO> devices = deviceService.getAllDevicesByUserId(userId);
		return ResponseEntity.ok(devices);
	}
}
