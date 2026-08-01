package com.dawak.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.dawak.api.identity.application.OtpDeliveryPort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));
	}

	@Bean
	@Primary
	CapturingOtpDelivery capturingOtpDelivery() {
		return new CapturingOtpDelivery();
	}
}
