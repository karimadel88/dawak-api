package com.dawak.api.prescription.api;

import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.prescription.api.dto.PrescriptionResponse;
import com.dawak.api.prescription.api.dto.ReviewRequest;
import com.dawak.api.prescription.application.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacist/prescriptions")
@PreAuthorize("hasRole('PHARMACIST')")
public class PharmacistPrescriptionController {
    private final PrescriptionService service;
    public PharmacistPrescriptionController(PrescriptionService service) { this.service = service; }

    @GetMapping("/queue")
    List<PrescriptionResponse> queue() { return service.unassignedQueue(); }

    @PostMapping("/{id}/claim")
    PrescriptionResponse claim(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.claim(id, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/{id}/review")
    PrescriptionResponse review(@PathVariable UUID id, @Valid @RequestBody ReviewRequest body,
                                @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.review(id, body, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }
}
