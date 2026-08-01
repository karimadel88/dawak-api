package com.dawak.api.patient.api;

import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.patient.api.dto.ConsentResponse;
import com.dawak.api.patient.api.dto.ProfileCompletionRequest;
import com.dawak.api.patient.api.dto.ProfileResponse;
import com.dawak.api.patient.api.dto.ProfileUpdateRequest;
import com.dawak.api.patient.application.PatientProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/profile")
public class PatientProfileController {
    private final PatientProfileService profiles;

    public PatientProfileController(PatientProfileService profiles) { this.profiles = profiles; }

    @PostMapping("/complete")
    ProfileResponse complete(@AuthenticationPrincipal Jwt jwt,
                             @Valid @RequestBody ProfileCompletionRequest body,
                             HttpServletRequest request) {
        return profiles.complete(AuthenticatedUser.userId(jwt), body, RequestMetadata.from(request));
    }

    @GetMapping
    ProfileResponse get(@AuthenticationPrincipal Jwt jwt) {
        return profiles.get(AuthenticatedUser.userId(jwt));
    }

    @PatchMapping
    ProfileResponse update(@AuthenticationPrincipal Jwt jwt,
                           @Valid @RequestBody ProfileUpdateRequest body,
                           HttpServletRequest request) {
        return profiles.update(AuthenticatedUser.userId(jwt), body, RequestMetadata.from(request));
    }

    @GetMapping("/consents")
    List<ConsentResponse> consents(@AuthenticationPrincipal Jwt jwt) {
        return profiles.consents(AuthenticatedUser.userId(jwt));
    }
}
