package com.dawak.api.catalogue.api;

import com.dawak.api.catalogue.api.dto.CatalogueImportRequest;
import com.dawak.api.catalogue.api.dto.CatalogueImportResponse;
import com.dawak.api.catalogue.api.dto.MedicinePackageResponse;
import com.dawak.api.catalogue.api.dto.MedicinePackageWriteRequest;
import com.dawak.api.catalogue.application.CatalogueService;
import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalogue")
@PreAuthorize("hasRole('CATALOGUE_MANAGER')")
public class AdminCatalogueController {
    private final CatalogueService catalogue;

    public AdminCatalogueController(CatalogueService catalogue) {
        this.catalogue = catalogue;
    }

    @PostMapping("/packages")
    @ResponseStatus(HttpStatus.CREATED)
    MedicinePackageResponse create(@Valid @RequestBody MedicinePackageWriteRequest body,
                                   @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return catalogue.create(body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PutMapping("/packages/{packageId}")
    MedicinePackageResponse update(@PathVariable UUID packageId,
                                   @Valid @RequestBody MedicinePackageWriteRequest body,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return catalogue.update(packageId, body, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @DeleteMapping("/packages/{packageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable UUID packageId,
                    @RequestParam(defaultValue = "Deactivated by catalogue manager") String reason,
                    @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        catalogue.deactivate(packageId, reason, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/imports")
    @ResponseStatus(HttpStatus.CREATED)
    CatalogueImportResponse importPackages(@Valid @RequestBody CatalogueImportRequest body,
                                           @RequestHeader("Idempotency-Key") String idempotencyKey,
                                           @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return catalogue.importPackages(body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }
}
