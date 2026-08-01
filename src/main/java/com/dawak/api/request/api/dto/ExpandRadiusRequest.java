package com.dawak.api.request.api.dto;

import jakarta.validation.constraints.Positive;

public record ExpandRadiusRequest(@Positive int radiusKm) {}
