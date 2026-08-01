package com.dawak.api.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("dawak.auth")
public record AuthProperties(
        Duration otpTtl,
        int otpMaxAttempts,
        int otpRequestLimit,
        Duration otpRequestWindow,
        String otpPepper,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String jwtSecret
) {
}
