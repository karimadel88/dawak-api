package com.dawak.api.prescription.api;

import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.prescription.api.dto.*;
import com.dawak.api.prescription.application.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {
    private final PrescriptionService service;
    public PrescriptionController(PrescriptionService service) { this.service = service; }

    @PostMapping("/upload-intents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATIENT')")
    UploadIntentResponse createIntent(@Valid @RequestBody UploadIntentRequest body,
                                      @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.createUploadIntent(body, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PutMapping(value = "/{id}/content", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PATIENT')")
    void upload(@PathVariable UUID id, @RequestHeader("Upload-Token") String uploadToken,
                @RequestBody byte[] content, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        service.upload(id, uploadToken, content, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('PATIENT')")
    PrescriptionResponse finalizeUpload(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                        HttpServletRequest request) {
        return service.finalizeUpload(id, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @GetMapping("/{id}")
    PrescriptionResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.get(id, AuthenticatedUser.userId(jwt));
    }

    @PostMapping("/{id}/access-url")
    AccessUrlResponse accessUrl(@PathVariable UUID id, @Valid @RequestBody AccessUrlRequest body,
                                @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.createAccessGrant(id, body, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
    }

    @GetMapping("/{id}/content")
    ResponseEntity<byte[]> content(@PathVariable UUID id, @RequestParam String accessToken,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        var file = service.readContent(id, accessToken, AuthenticatedUser.userId(jwt), RequestMetadata.from(request));
        String safeName = file.filename().replace("\"", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" +
                        java.net.URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20"))
                .cacheControl(CacheControl.noStore())
                .body(file.bytes());
    }
}
