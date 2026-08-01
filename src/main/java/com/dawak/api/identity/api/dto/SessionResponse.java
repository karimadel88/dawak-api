package com.dawak.api.identity.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String deviceId,
        String deviceName,
        String ipAddress,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        boolean current,
        boolean revoked
) {
}
