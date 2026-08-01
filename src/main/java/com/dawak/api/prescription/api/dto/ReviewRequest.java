package com.dawak.api.prescription.api.dto;

import com.dawak.api.prescription.domain.ReviewDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ReviewRequest(
        @NotNull ReviewDecision decision,
        @Size(max = 80) String reasonCode,
        @Size(max = 500) String comment,
        Instant validUntil
) {}
