package com.energytracker.user.testsupport;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mysql.MySQLContainer;

public abstract class MySqlTestcontainersBase {

	@ServiceConnection
	@Container
	static MySQLContainer mysql = new MySQLContainer("mysql:8.3.0")
		.withDatabaseName("energy_tracker")
		.withUsername("root")
		.withPassword("password");
}
