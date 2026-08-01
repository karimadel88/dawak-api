package com.dawak.api.prescription.api.dto;

import java.time.Instant;

public record AccessUrlResponse(String accessUrl, String accessToken, Instant expiresAt) {}
