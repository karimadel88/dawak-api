package com.dawak.api.patient.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProfileCompletionRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Email @Size(max = 254) String email,
        @Min(1900) @Max(2100) Integer birthYear,
        @NotBlank @Pattern(regexp = "ar|en") String preferredLanguage,
        @NotNull UUID cityId,
        @NotNull UUID areaId,
        @NotBlank @Size(max = 40) String acceptedTermsVersion,
        @NotBlank @Size(max = 40) String acceptedPrivacyVersion
) {
}
