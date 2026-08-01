package com.dawak.api.prescription.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.domain.User;
import com.dawak.api.identity.persistence.UserRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import com.dawak.api.prescription.api.dto.*;
import com.dawak.api.prescription.config.PrescriptionProperties;
import com.dawak.api.prescription.domain.*;
import com.dawak.api.prescription.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);
    private final PrescriptionRepository prescriptions;
    private final PrescriptionAccessGrantRepository grants;
    private final PrescriptionAccessLogRepository accessLogs;
    private final PatientProfileRepository patients;
    private final UserRepository users;
    private final PrescriptionStorage storage;
    private final PrescriptionFileInspector inspector;
    private final PrescriptionProperties properties;
    private final AuditService audit;
    private final SecureRandom random = new SecureRandom();

    public PrescriptionService(PrescriptionRepository prescriptions, PrescriptionAccessGrantRepository grants,
                               PrescriptionAccessLogRepository accessLogs, PatientProfileRepository patients,
                               UserRepository users, PrescriptionStorage storage, PrescriptionFileInspector inspector,
                               PrescriptionProperties properties, AuditService audit) {
        this.prescriptions = prescriptions; this.grants = grants; this.accessLogs = accessLogs;
        this.patients = patients; this.users = users; this.storage = storage; this.inspector = inspector;
        this.properties = properties; this.audit = audit;
    }

    @Transactional
    public UploadIntentResponse createUploadIntent(UploadIntentRequest request, UUID userId, RequestMetadata metadata) {
        if (request.fileSize() > properties.maxFileSize()) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "PRESCRIPTION_FILE_TOO_LARGE",
                    "Prescription exceeds the maximum allowed file size.");
        }
        var patient = patients.findByUserId(userId).orElseThrow(() ->
                new ApiException(HttpStatus.FORBIDDEN, "PATIENT_PROFILE_REQUIRED", "Complete the patient profile before uploading."));
        String token = token();
        Instant now = Instant.now();
        var prescription = prescriptions.save(new Prescription(patient, request.medicinePackageId(), UUID.randomUUID().toString(),
                sanitizeFilename(request.originalFilename()), request.contentType(), request.fileSize(),
                request.checksumSha256().toLowerCase(), PrescriptionFileInspector.sha256(token.getBytes()),
                now.plus(properties.uploadTtl()), now.plus(properties.retentionPeriod())));
        audit.record(patient.getUser(), "PRESCRIPTION_UPLOAD_INTENT_CREATED", "PRESCRIPTION", prescription.getId(),
                "SUCCESS", metadata, "status=UPLOAD_PENDING");
        log.info("Prescription upload intent created prescriptionId={} patientUserId={} contentType={} expectedBytes={} expiresAt={}",
                prescription.getId(), userId, request.contentType(), request.fileSize(), prescription.getUploadExpiresAt());
        return new UploadIntentResponse(prescription.getId(),
                "/api/v1/prescriptions/" + prescription.getId() + "/content", token, prescription.getUploadExpiresAt());
    }

    @Transactional
    public void upload(UUID id, String uploadToken, byte[] content, UUID userId, RequestMetadata metadata) {
        Prescription value = own(id, userId);
        log.info("Prescription content upload started prescriptionId={} patientUserId={} status={} expectedBytes={} receivedBytes={}",
                id, userId, value.getStatus(), value.getFileSize(), content.length);
        if (value.getStatus() != PrescriptionStatus.UPLOAD_PENDING || value.getUploadExpiresAt() == null
                || !value.getUploadExpiresAt().isAfter(Instant.now()) || uploadToken == null
                || !PrescriptionFileInspector.sha256(uploadToken.getBytes()).equals(value.getUploadTokenHash())) {
            log.warn("Prescription content upload rejected prescriptionId={} code=PRESCRIPTION_UPLOAD_TOKEN_INVALID status={} tokenPresent={} tokenExpired={}",
                    id, value.getStatus(), uploadToken != null, value.getUploadExpiresAt() != null && !value.getUploadExpiresAt().isAfter(Instant.now()));
            throw new ApiException(HttpStatus.FORBIDDEN, "PRESCRIPTION_UPLOAD_TOKEN_INVALID", "Upload token is invalid or expired.");
        }
        if (content.length != value.getFileSize() || content.length > properties.maxFileSize()) {
            log.warn("Prescription content upload rejected prescriptionId={} code=PRESCRIPTION_FILE_SIZE_MISMATCH expectedBytes={} receivedBytes={} maxBytes={}",
                    id, value.getFileSize(), content.length, properties.maxFileSize());
            throw new ApiException(HttpStatus.BAD_REQUEST, "PRESCRIPTION_FILE_SIZE_MISMATCH", "Uploaded content size does not match the upload intent.");
        }
        try {
            storage.write(value.getStorageKey(), content);
        } catch (ApiException exception) {
            log.error("Prescription content upload dependency failure prescriptionId={} phase=STORAGE_WRITE code={}",
                    id, exception.getCode(), exception);
            throw exception;
        }
        value.quarantine(Instant.now());
        audit.record(value.getPatientProfile().getUser(), "PRESCRIPTION_UPLOADED", "PRESCRIPTION", id,
                "SUCCESS", metadata, "status=QUARANTINED");
        log.info("Prescription content upload completed prescriptionId={} status=QUARANTINED receivedBytes={}", id, content.length);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public PrescriptionResponse finalizeUpload(UUID id, UUID userId, RequestMetadata metadata) {
        Prescription value = own(id, userId);
        if (value.getStatus() != PrescriptionStatus.QUARANTINED) {
            log.warn("Prescription finalization rejected prescriptionId={} code=PRESCRIPTION_NOT_QUARANTINED status={}",
                    id, value.getStatus());
            throw new ApiException(HttpStatus.CONFLICT, "PRESCRIPTION_NOT_QUARANTINED", "Prescription is not awaiting validation.");
        }
        log.info("Prescription finalization started prescriptionId={} contentType={} expectedBytes={}",
                id, value.getDeclaredContentType(), value.getFileSize());
        try {
            byte[] content = storage.read(value.getStorageKey());
            String detected = inspector.inspect(content, value.getDeclaredContentType(), value.getChecksumSha256());
            value.scanPassed(detected);
            audit.record(value.getPatientProfile().getUser(), "PRESCRIPTION_SCAN_PASSED", "PRESCRIPTION", id,
                    "SUCCESS", metadata, "status=PENDING_REVIEW");
            log.info("Prescription finalization completed prescriptionId={} status=PENDING_REVIEW detectedContentType={}",
                    id, detected);
            return PrescriptionResponse.from(value);
        } catch (ApiException exception) {
            if (exception.getStatus().is5xxServerError()) {
                audit.record(value.getPatientProfile().getUser(), "PRESCRIPTION_SCAN_DEFERRED", "PRESCRIPTION", id,
                        "ERROR", metadata, "code=" + exception.getCode());
                log.error("Prescription finalization dependency failure prescriptionId={} phase=SCAN_OR_STORAGE code={} statusPreserved=QUARANTINED",
                        id, exception.getCode(), exception);
                throw exception;
            }
            value.scanFailed();
            storage.delete(value.getStorageKey());
            audit.record(value.getPatientProfile().getUser(), "PRESCRIPTION_SCAN_FAILED", "PRESCRIPTION", id,
                    "DENIED", metadata, "code=" + exception.getCode());
            log.warn("Prescription finalization rejected prescriptionId={} code={} status=SCAN_FAILED quarantinedObjectDeleted=true",
                    id, exception.getCode());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse get(UUID id, UUID userId) {
        Prescription value = getOne(id);
        authorizeMetadata(value, userId);
        return PrescriptionResponse.from(value);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> unassignedQueue() {
        return prescriptions.findByStatusAndReviewerIsNullOrderByCreatedAtAsc(PrescriptionStatus.PENDING_REVIEW)
                .stream().map(PrescriptionResponse::from).toList();
    }

    @Transactional
    public PrescriptionResponse claim(UUID id, UUID pharmacistId, RequestMetadata metadata) {
        Prescription value = getOneForUpdate(id);
        User pharmacist = user(pharmacistId);
        try { value.claim(pharmacist); }
        catch (IllegalStateException exception) { throw conflict("PRESCRIPTION_CANNOT_BE_CLAIMED", exception.getMessage()); }
        audit.record(pharmacist, "PRESCRIPTION_REVIEW_CLAIMED", "PRESCRIPTION", id, "SUCCESS", metadata,
                "purpose=PRESCRIPTION_REVIEW");
        return PrescriptionResponse.from(value);
    }

    @Transactional
    public AccessUrlResponse createAccessGrant(UUID id, AccessUrlRequest request, UUID userId, RequestMetadata metadata) {
        Prescription value = getOne(id);
        authorizeContent(value, userId);
        if (value.getStatus() == PrescriptionStatus.UPLOAD_PENDING || value.getStatus() == PrescriptionStatus.QUARANTINED
                || value.getStatus() == PrescriptionStatus.SCAN_FAILED || value.getStatus() == PrescriptionStatus.DELETED) {
            throw new ApiException(HttpStatus.CONFLICT, "PRESCRIPTION_CONTENT_UNAVAILABLE", "Prescription content is not available.");
        }
        User actor = user(userId);
        String token = token();
        Instant expiresAt = Instant.now().plus(properties.accessTtl());
        grants.save(new PrescriptionAccessGrant(value, actor, PrescriptionFileInspector.sha256(token.getBytes()),
                request.purpose(), expiresAt, Instant.now()));
        accessLogs.save(new PrescriptionAccessLog(value, actor, "ACCESS_GRANTED", request.purpose(),
                metadata.ipAddress(), metadata.userAgent(), Instant.now()));
        audit.record(actor, "PRESCRIPTION_ACCESS_GRANTED", "PRESCRIPTION", id, "SUCCESS", metadata,
                "purpose=" + request.purpose());
        return new AccessUrlResponse("/api/v1/prescriptions/" + id + "/content?accessToken=" + token, token, expiresAt);
    }

    @Transactional
    public FileContent readContent(UUID id, String accessToken, UUID userId, RequestMetadata metadata) {
        String hash = PrescriptionFileInspector.sha256(accessToken.getBytes());
        PrescriptionAccessGrant grant = grants.findByTokenHash(hash).orElseThrow(() ->
                new ApiException(HttpStatus.FORBIDDEN, "PRESCRIPTION_ACCESS_TOKEN_INVALID", "Access token is invalid or expired."));
        if (!grant.getPrescription().getId().equals(id) || !grant.getUser().getId().equals(userId)
                || !grant.getExpiresAt().isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PRESCRIPTION_ACCESS_TOKEN_INVALID", "Access token is invalid or expired.");
        }
        authorizeContent(grant.getPrescription(), userId);
        grant.use(Instant.now());
        accessLogs.save(new PrescriptionAccessLog(grant.getPrescription(), grant.getUser(), "CONTENT_READ", grant.getPurpose(),
                metadata.ipAddress(), metadata.userAgent(), Instant.now()));
        audit.record(grant.getUser(), "PRESCRIPTION_CONTENT_READ", "PRESCRIPTION", id, "SUCCESS", metadata,
                "purpose=" + grant.getPurpose());
        return new FileContent(storage.read(grant.getPrescription().getStorageKey()),
                grant.getPrescription().getDetectedContentType(), grant.getPrescription().getOriginalFilename());
    }

    @Transactional
    public PrescriptionResponse review(UUID id, ReviewRequest request, UUID pharmacistId, RequestMetadata metadata) {
        Prescription value = getOneForUpdate(id);
        if (value.getReviewer() == null || !value.getReviewer().getId().equals(pharmacistId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PRESCRIPTION_REVIEWER_NOT_DESIGNATED", "Only the designated pharmacist may review this prescription.");
        }
        if (request.decision() != ReviewDecision.APPROVE && (request.reasonCode() == null || request.reasonCode().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PRESCRIPTION_REVIEW_REASON_REQUIRED", "A reason is required for rejection or clarification.");
        }
        if (request.decision() == ReviewDecision.APPROVE && request.validUntil() != null
                && !request.validUntil().isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PRESCRIPTION_VALIDITY_INVALID", "Prescription validity must be in the future.");
        }
        try { value.review(request.decision(), request.reasonCode(), request.comment(), request.validUntil(), Instant.now()); }
        catch (IllegalStateException exception) { throw conflict("PRESCRIPTION_CANNOT_BE_REVIEWED", exception.getMessage()); }
        User actor = user(pharmacistId);
        accessLogs.save(new PrescriptionAccessLog(value, actor, "REVIEW_DECISION", "PRESCRIPTION_REVIEW",
                metadata.ipAddress(), metadata.userAgent(), Instant.now()));
        audit.record(actor, "PRESCRIPTION_REVIEW_DECIDED", "PRESCRIPTION", id, "SUCCESS", metadata,
                "decision=" + request.decision() + ";reason=" + request.reasonCode());
        return PrescriptionResponse.from(value);
    }

    private Prescription own(UUID id, UUID userId) {
        Prescription value = getOne(id);
        if (!value.getPatientProfile().getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found.");
        }
        return value;
    }
    private void authorizeMetadata(Prescription value, UUID userId) {
        if (value.getPatientProfile().getUser().getId().equals(userId)) return;
        if (value.getReviewer() != null && value.getReviewer().getId().equals(userId)) return;
        throw new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found.");
    }
    private void authorizeContent(Prescription value, UUID userId) { authorizeMetadata(value, userId); }
    private Prescription getOne(UUID id) { return prescriptions.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found.")); }
    private Prescription getOneForUpdate(UUID id) { return prescriptions.findByIdForUpdate(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found.")); }
    private User user(UUID id) { return users.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user was not found.")); }
    private ApiException conflict(String code, String message) { return new ApiException(HttpStatus.CONFLICT, code, message); }
    private String token() { byte[] bytes = new byte[32]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String sanitizeFilename(String name) {
        String sanitized = name.replace('\\', '_').replace('/', '_').replaceAll("[\\p{Cntrl}]", "_").trim();
        return sanitized.isBlank() ? "prescription" : sanitized;
    }
    public record FileContent(byte[] bytes, String contentType, String filename) {}
}
