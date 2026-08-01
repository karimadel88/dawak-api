package com.dawak.api.request.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("dawak.request")
public record MedicineRequestProperties(
        int maxQuantity,
        int initialRadiusKm,
        int maxRadiusKm,
        int matchingBatchSize,
        Duration ttl
) {}
