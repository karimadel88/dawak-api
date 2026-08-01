package com.dawak.api.catalogue.api.dto;

import com.dawak.api.catalogue.domain.MedicinePackageStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record MedicinePackageWriteRequest(
        @NotBlank @Size(max = 200) String nameAr,
        @NotBlank @Size(max = 200) String nameEn,
        @NotBlank @Size(max = 60) String manufacturerCode,
        @NotBlank @Size(max = 160) String manufacturerNameAr,
        @NotBlank @Size(max = 160) String manufacturerNameEn,
        @NotEmpty @Size(max = 10) List<@Valid IngredientInput> activeIngredients,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal strengthValue,
        @NotBlank @Size(max = 40) String strengthUnit,
        @NotBlank @Size(max = 80) String dosageFormCode,
        @NotBlank @Size(max = 120) String dosageFormNameAr,
        @NotBlank @Size(max = 120) String dosageFormNameEn,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal packageSizeValue,
        @NotBlank @Size(max = 40) String packageSizeUnit,
        @Size(max = 80) String routeOfAdministration,
        @Size(max = 80) String barcode,
        @DecimalMin("0") BigDecimal officialPrice,
        @Size(min = 3, max = 3) String currency,
        boolean prescriptionRequired,
        boolean restricted,
        @Size(max = 80) String restrictionCode,
        @Size(max = 80) String storageType,
        @NotNull MedicinePackageStatus status,
        @Size(max = 20) List<@NotBlank @Size(max = 200) String> aliases
) {
    public MedicinePackageWriteRequest {
        activeIngredients = activeIngredients == null ? List.of() : List.copyOf(activeIngredients);
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }
}
