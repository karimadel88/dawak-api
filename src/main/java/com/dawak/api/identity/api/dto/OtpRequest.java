package com.dawak.api.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpRequest(@NotBlank @Size(max = 30) String phoneNumber) {
}
