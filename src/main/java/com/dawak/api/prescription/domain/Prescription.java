package com.dawak.api.prescription.domain;

import com.dawak.api.common.persistence.MutableEntity;
import com.dawak.api.identity.domain.User;
import com.dawak.api.patient.domain.PatientProfile;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription")
public class Prescription extends MutableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;
    @Column(name = "storage_key", nullable = false, unique = true, length = 200)
    private String storageKey;
    @Column(name = "medicine_package_id")
    private UUID medicinePackageId;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(name = "declared_content_type", nullable = false, length = 100)
    private String declaredContentType;
    @Column(name = "detected_content_type", length = 100)
    private String detectedContentType;
    @Column(name = "file_size", nullable = false)
    private long fileSize;
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PrescriptionStatus status;
    @Column(name = "upload_token_hash", length = 64)
    private String uploadTokenHash;
    @Column(name = "upload_expires_at")
    private Instant uploadExpiresAt;
    @Column(name = "uploaded_at")
    private Instant uploadedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewer;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    @Column(name = "review_reason_code", length = 80)
    private String reviewReasonCode;
    @Column(name = "review_comment", length = 500)
    private String reviewComment;
    @Column(name = "valid_until")
    private Instant validUntil;
    @Column(name = "retention_until", nullable = false)
    private Instant retentionUntil;
    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Prescription() {}

    public Prescription(PatientProfile patientProfile, UUID medicinePackageId, String storageKey, String originalFilename,
                        String declaredContentType, long fileSize, String checksumSha256,
                        String uploadTokenHash, Instant uploadExpiresAt, Instant retentionUntil) {
        super(UUID.randomUUID());
        this.patientProfile = patientProfile;
        this.medicinePackageId = medicinePackageId;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.declaredContentType = declaredContentType;
        this.fileSize = fileSize;
        this.checksumSha256 = checksumSha256;
        this.uploadTokenHash = uploadTokenHash;
        this.uploadExpiresAt = uploadExpiresAt;
        this.retentionUntil = retentionUntil;
        this.status = PrescriptionStatus.UPLOAD_PENDING;
    }

    public void quarantine(Instant now) {
        require(PrescriptionStatus.UPLOAD_PENDING);
        status = PrescriptionStatus.QUARANTINED;
        uploadedAt = now;
        uploadTokenHash = null;
        uploadExpiresAt = null;
    }

    public void scanPassed(String detectedContentType) {
        require(PrescriptionStatus.QUARANTINED);
        this.detectedContentType = detectedContentType;
        status = PrescriptionStatus.PENDING_REVIEW;
    }

    public void scanFailed() {
        require(PrescriptionStatus.QUARANTINED);
        status = PrescriptionStatus.SCAN_FAILED;
    }

    public void claim(User pharmacist) {
        require(PrescriptionStatus.PENDING_REVIEW);
        if (reviewer != null && !reviewer.getId().equals(pharmacist.getId())) {
            throw new IllegalStateException("Prescription already has a designated reviewer");
        }
        reviewer = pharmacist;
    }

    public void review(ReviewDecision decision, String reasonCode, String comment, Instant validUntil, Instant now) {
        require(PrescriptionStatus.PENDING_REVIEW);
        if (reviewer == null) throw new IllegalStateException("A designated reviewer is required");
        status = switch (decision) {
            case APPROVE -> PrescriptionStatus.APPROVED;
            case REJECT -> PrescriptionStatus.REJECTED;
            case REQUEST_CLARIFICATION -> PrescriptionStatus.NEEDS_CLARIFICATION;
        };
        this.reviewReasonCode = reasonCode;
        this.reviewComment = comment;
        this.validUntil = decision == ReviewDecision.APPROVE ? validUntil : null;
        this.reviewedAt = now;
    }

    public void deleteForRetention(Instant now) {
        if (status == PrescriptionStatus.DELETED) return;
        status = PrescriptionStatus.DELETED;
        storageKey = "deleted-" + getId();
        originalFilename = "[deleted]";
        declaredContentType = "application/octet-stream";
        detectedContentType = null;
        checksumSha256 = "0".repeat(64);
        uploadTokenHash = null;
        uploadExpiresAt = null;
        reviewComment = null;
        deletedAt = now;
    }

    private void require(PrescriptionStatus expected) {
        if (status != expected) throw new IllegalStateException("Expected " + expected + " but was " + status);
    }

    public PatientProfile getPatientProfile() { return patientProfile; }
    public UUID getMedicinePackageId() { return medicinePackageId; }
    public String getStorageKey() { return storageKey; }
    public String getOriginalFilename() { return originalFilename; }
    public String getDeclaredContentType() { return declaredContentType; }
    public String getDetectedContentType() { return detectedContentType; }
    public long getFileSize() { return fileSize; }
    public String getChecksumSha256() { return checksumSha256; }
    public PrescriptionStatus getStatus() { return status; }
    public String getUploadTokenHash() { return uploadTokenHash; }
    public Instant getUploadExpiresAt() { return uploadExpiresAt; }
    public Instant getUploadedAt() { return uploadedAt; }
    public User getReviewer() { return reviewer; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getReviewReasonCode() { return reviewReasonCode; }
    public String getReviewComment() { return reviewComment; }
    public Instant getValidUntil() { return validUntil; }
    public Instant getRetentionUntil() { return retentionUntil; }
}
