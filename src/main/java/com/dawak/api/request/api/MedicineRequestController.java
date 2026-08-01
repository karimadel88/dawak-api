package com.dawak.api.request.api;

import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.request.api.dto.CancelMedicineRequest;
import com.dawak.api.request.api.dto.CreateMedicineRequest;
import com.dawak.api.request.api.dto.ExpandRadiusRequest;
import com.dawak.api.request.api.dto.MedicineRequestResponse;
import com.dawak.api.request.api.dto.QualifyMedicineRequest;
import com.dawak.api.request.application.MedicineRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medicine-requests")
@PreAuthorize("hasRole('PATIENT')")
public class MedicineRequestController {
    private final MedicineRequestService service;

    public MedicineRequestController(MedicineRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MedicineRequestResponse create(@Valid @RequestBody CreateMedicineRequest body,
                                   @RequestHeader("Idempotency-Key") String idempotencyKey,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.create(body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @GetMapping
    List<MedicineRequestResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(AuthenticatedUser.userId(jwt));
    }

    @GetMapping("/{id}")
    MedicineRequestResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.get(id, AuthenticatedUser.userId(jwt));
    }

    @PostMapping("/{id}/submit")
    MedicineRequestResponse submit(@PathVariable UUID id,
                                   @RequestHeader("Idempotency-Key") String idempotencyKey,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.submit(id, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/{id}/qualify")
    MedicineRequestResponse qualify(@PathVariable UUID id, @Valid @RequestBody QualifyMedicineRequest body,
                                    @RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.qualify(id, body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/{id}/expand-radius")
    MedicineRequestResponse expandRadius(@PathVariable UUID id, @Valid @RequestBody ExpandRadiusRequest body,
                                         @RequestHeader("Idempotency-Key") String idempotencyKey,
                                         @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.expandRadius(id, body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/{id}/cancel")
    MedicineRequestResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelMedicineRequest body,
                                   @RequestHeader("Idempotency-Key") String idempotencyKey,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.cancel(id, body, idempotencyKey, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }
}
