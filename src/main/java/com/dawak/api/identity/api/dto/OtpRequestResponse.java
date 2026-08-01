package com.dawak.api.identity.api.dto;

import java.util.UUID;

public record OtpRequestResponse(UUID challengeId, long expiresInSeconds, long resendAfterSeconds) {
}
