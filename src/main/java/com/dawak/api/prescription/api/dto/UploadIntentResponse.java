package com.dawak.api.prescription.api.dto;

import java.time.Instant;
import java.util.UUID;

public record UploadIntentResponse(UUID prescriptionId, String uploadUrl, String uploadToken, Instant expiresAt) {}
