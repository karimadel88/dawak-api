package com.dawak.api.request.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelMedicineRequest(
        @NotBlank @Size(max = 80) String reasonCode,
        @Size(max = 500) String reasonText
) {}
