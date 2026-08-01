package com.dawak.api.patient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("dawak.policy")
public record PolicyProperties(String termsVersion, String privacyVersion) {
}
