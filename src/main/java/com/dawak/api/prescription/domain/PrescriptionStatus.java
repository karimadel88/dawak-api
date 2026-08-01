package com.dawak.api.prescription.domain;

public enum PrescriptionStatus {
    UPLOAD_PENDING,
    QUARANTINED,
    PENDING_REVIEW,
    NEEDS_CLARIFICATION,
    APPROVED,
    REJECTED,
    SCAN_FAILED,
    EXPIRED,
    RETENTION_EXPIRED,
    DELETED
}
