package com.dawak.api.prescription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("dawak.prescription")
public record PrescriptionProperties(
        long maxFileSize,
        Duration uploadTtl,
        Duration accessTtl,
        Duration retentionPeriod,
        String storagePath,
        String encryptionSecret,
        String storageType,
        String scannerType,
        String minioEndpoint,
        String minioAccessKey,
        String minioSecretKey,
        String minioBucket,
        boolean minioCreateBucket,
        String clamavHost,
        int clamavPort,
        Duration clamavConnectTimeout,
        Duration clamavReadTimeout
) {}
