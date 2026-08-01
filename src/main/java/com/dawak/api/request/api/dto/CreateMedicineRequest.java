package com.dawak.api.request.api.dto;

import com.dawak.api.request.domain.FulfillmentPreference;
import com.dawak.api.request.domain.RequestUrgency;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateMedicineRequest(
        @NotNull UUID medicinePackageId,
        @Min(1) int quantity,
        @NotNull UUID cityId,
        @NotNull UUID areaId,
        @NotNull FulfillmentPreference fulfillmentPreference,
        @NotNull RequestUrgency urgency,
        UUID prescriptionId,
        @Size(max = 500) String notes
) {}
