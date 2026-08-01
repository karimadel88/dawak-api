package com.dawak.api.catalogue.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IngredientInput(
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 160) String nameAr,
        @NotBlank @Size(max = 160) String nameEn
) {}
