package com.energytracker.alert_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energytracker.alert_service.entity.Alert;

// @Repository - not needed, spring boot is smart enough to know this is a repository interface
public interface AlertRepository extends JpaRepository<Alert, Long> {

}
