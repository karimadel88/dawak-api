package com.dawak.api.catalogue.api.dto;

import com.dawak.api.catalogue.domain.MedicinePackageStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MedicinePackageResponse(
        UUID id,
        UUID medicineId,
        String nameAr,
        String nameEn,
        UUID manufacturerId,
        String manufacturerNameAr,
        String manufacturerNameEn,
        List<IngredientResponse> activeIngredients,
        BigDecimal strengthValue,
        String strengthUnit,
        String dosageFormCode,
        String dosageFormNameAr,
        String dosageFormNameEn,
        BigDecimal packageSizeValue,
        String packageSizeUnit,
        String routeOfAdministration,
        String barcode,
        BigDecimal officialPrice,
        String currency,
        boolean prescriptionRequired,
        boolean restricted,
        String storageType,
        MedicinePackageStatus status,
        boolean active,
        boolean requestable,
        String unavailableReason,
        Instant updatedAt
) {
    public record IngredientResponse(UUID id, String code, String nameAr, String nameEn) {}
}
