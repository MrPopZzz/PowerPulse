package com.energytracker.device_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.energytracker.device_service.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

	List<Device> findAllByUserId(Long userId);
}
