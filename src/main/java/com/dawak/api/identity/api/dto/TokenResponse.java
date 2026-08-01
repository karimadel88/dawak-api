package com.dawak.api.identity.api.dto;

import java.util.UUID;

public record TokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        UUID sessionId,
        boolean onboardingRequired
) {
}
