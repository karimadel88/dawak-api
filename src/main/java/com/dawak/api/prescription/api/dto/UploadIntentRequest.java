package com.dawak.api.prescription.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UploadIntentRequest(
        @NotBlank @Size(max = 255) String originalFilename,
        @NotBlank @Pattern(regexp = "application/pdf|image/jpeg|image/png") String contentType,
        @Positive long fileSize,
        @NotBlank @Pattern(regexp = "[0-9a-fA-F]{64}") String checksumSha256,
        UUID medicinePackageId
) {}
