package com.dawak.api.prescription.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccessUrlRequest(@NotBlank @Size(max = 80) String purpose) {}
