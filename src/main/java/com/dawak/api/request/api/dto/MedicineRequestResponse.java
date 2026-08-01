package com.dawak.api.request.api.dto;

import com.dawak.api.request.domain.MedicineRequest;

import java.time.Instant;
import java.util.UUID;

public record MedicineRequestResponse(
        UUID id, String referenceNumber, String status, UUID medicinePackageId, int quantity,
        UUID prescriptionId, UUID cityId, UUID areaId, String fulfillmentPreference, String urgency,
        int searchRadiusKm, int matchedBranchCount, Instant submittedAt, Instant expiresAt,
        Instant createdAt, Instant updatedAt, boolean replayed
) {
    public static MedicineRequestResponse from(MedicineRequest value, int matches, boolean replayed) {
        return new MedicineRequestResponse(value.getId(), value.getReferenceNumber(), value.getStatus().name(),
                value.getItem().getMedicinePackageId(), value.getItem().getRequestedQuantity(),
                value.getPrescription() == null ? null : value.getPrescription().getId(),
                value.getCity().getId(), value.getArea().getId(), value.getFulfillmentPreference().name(),
                value.getUrgency().name(), value.getSearchRadiusKm(), matches, value.getSubmittedAt(),
                value.getExpiresAt(), value.getCreatedAt(), value.getUpdatedAt(), replayed);
    }
}
