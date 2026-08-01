package com.dawak.api.prescription.api.dto;

import com.dawak.api.prescription.domain.Prescription;

import java.time.Instant;
import java.util.UUID;

public record PrescriptionResponse(
        UUID id, String originalFilename, String contentType, long fileSize, String checksumSha256,
        String status, Instant uploadedAt, UUID designatedReviewerId, Instant reviewedAt,
        String reviewReasonCode, String reviewComment, Instant validUntil, Instant retentionUntil
) {
    public static PrescriptionResponse from(Prescription value) {
        return new PrescriptionResponse(value.getId(), value.getOriginalFilename(),
                value.getDetectedContentType() == null ? value.getDeclaredContentType() : value.getDetectedContentType(),
                value.getFileSize(), value.getChecksumSha256(), value.getStatus().name(), value.getUploadedAt(),
                value.getReviewer() == null ? null : value.getReviewer().getId(), value.getReviewedAt(),
                value.getReviewReasonCode(), value.getReviewComment(), value.getValidUntil(), value.getRetentionUntil());
    }
}
