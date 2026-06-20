package com.energytracker.device_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.energytracker.device_service.dto.DeviceDTO;
import com.energytracker.device_service.entity.Device;
import com.energytracker.device_service.exception.DeviceNotFoundException;
import com.energytracker.device_service.repository.DeviceRepository;

@Service
public class DeviceService {

	private DeviceRepository deviceRepository;
	
	public DeviceService(DeviceRepository deviceRepository) {
		this.deviceRepository = deviceRepository;
	}
	
	public DeviceDTO getDeviceById(Long id) {
		Device device = deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
		
		return toDTO(device);
	}
	
	public DeviceDTO createDevice(DeviceDTO input) {
		Device device = new Device();
		device.setName(input.getName());
		device.setType(input.getType());
		device.setLocation(input.getLocation());
		device.setUserId(input.getUserId());
		
		Device savedDevice = deviceRepository.save(device);
		
		return toDTO(savedDevice);
	}
	
	public DeviceDTO updateDevice(Long id, DeviceDTO input) {
		Device existingDevice = deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
		
		existingDevice.setName(input.getName());
		existingDevice.setType(input.getType());
		existingDevice.setLocation(input.getLocation());
		existingDevice.setUserId(input.getUserId());
		
		final Device savedDevice = deviceRepository.save(existingDevice);
		return toDTO(savedDevice);
	}
	
	public void deleteDevice(Long id) {
		if(!deviceRepository.existsById(id)) {
			throw new DeviceNotFoundException("Device not found with id: " + id);
		}
		
		deviceRepository.deleteById(id);
	}
	
	public List<DeviceDTO> getAllDevicesByUserId(Long userId) {
		List<Device> devices = deviceRepository.findAllByUserId(userId);
		return devices.stream()
				.map(this::toDTO)
				.toList();
	}
	
	
	
	
	// Helper methods
	private DeviceDTO toDTO(Device device) {
		return DeviceDTO.builder()
				.id(device.getId())
				.name(device.getName())
				.type(device.getType())
				.location(device.getLocation())
				.userId(device.getUserId())
				.build();
	}
}
