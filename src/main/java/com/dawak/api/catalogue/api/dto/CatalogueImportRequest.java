package com.dawak.api.catalogue.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CatalogueImportRequest(
        @NotEmpty @Size(max = 500) List<@Valid MedicinePackageWriteRequest> packages
) {
    public CatalogueImportRequest {
        packages = packages == null ? List.of() : List.copyOf(packages);
    }
}
