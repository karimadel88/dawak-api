package com.dawak.api.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OtpVerificationRequest(
        @NotNull UUID challengeId,
        @NotBlank @Size(max = 30) String phoneNumber,
        @NotBlank @Pattern(regexp = "\\d{6}") String code,
        @NotBlank @Size(max = 200) String deviceId,
        @Size(max = 200) String deviceName
) {
}
