package com.dawak.api.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class AuthenticatedUser {
    private AuthenticatedUser() {}

    public static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public static UUID sessionId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("sid"));
    }
}
