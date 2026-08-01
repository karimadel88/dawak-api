package com.dawak.api.catalogue.api;

import com.dawak.api.catalogue.api.dto.MedicinePackageResponse;
import com.dawak.api.catalogue.api.dto.PageResponse;
import com.dawak.api.catalogue.application.CatalogueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medicines")
public class MedicineController {
    private final CatalogueService catalogue;

    public MedicineController(CatalogueService catalogue) {
        this.catalogue = catalogue;
    }

    @GetMapping("/search")
    PageResponse<MedicinePackageResponse> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String dosageForm,
            @RequestParam(required = false) UUID manufacturerId,
            @RequestParam(required = false) Boolean prescriptionRequired) {
        return catalogue.search(q, page, size, dosageForm, manufacturerId, prescriptionRequired);
    }

    @GetMapping("/{medicinePackageId}")
    MedicinePackageResponse detail(@PathVariable UUID medicinePackageId) {
        return catalogue.find(medicinePackageId);
    }
}
