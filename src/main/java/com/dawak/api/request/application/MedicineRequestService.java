package com.dawak.api.request.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.domain.User;
import com.dawak.api.identity.persistence.UserRepository;
import com.dawak.api.patient.persistence.AreaRepository;
import com.dawak.api.patient.persistence.CityRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import com.dawak.api.prescription.domain.Prescription;
import com.dawak.api.prescription.domain.PrescriptionStatus;
import com.dawak.api.prescription.persistence.PrescriptionRepository;
import com.dawak.api.request.api.dto.*;
import com.dawak.api.request.config.MedicineRequestProperties;
import com.dawak.api.request.domain.*;
import com.dawak.api.request.persistence.MedicineRequestRepository;
import com.dawak.api.request.persistence.MedicineRequestStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class MedicineRequestService {
    private static final Logger log = LoggerFactory.getLogger(MedicineRequestService.class);
    private static final RequestMetadata SYSTEM_METADATA = new RequestMetadata(null, null);
    private static final List<MedicineRequestStatus> EXPIRABLE_STATUSES = List.of(
            MedicineRequestStatus.SUBMITTED, MedicineRequestStatus.AWAITING_PRESCRIPTION,
            MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW, MedicineRequestStatus.READY_FOR_MATCHING,
            MedicineRequestStatus.MATCHING, MedicineRequestStatus.OFFERS_AVAILABLE,
            MedicineRequestStatus.UNFULFILLED);
    private final MedicineRequestRepository requests;
    private final MedicineRequestStatusHistoryRepository history;
    private final PatientProfileRepository patients;
    private final UserRepository users;
    private final CityRepository cities;
    private final AreaRepository areas;
    private final PrescriptionRepository prescriptions;
    private final JdbcClient jdbc;
    private final MedicineRequestProperties properties;
    private final AuditService audit;

    public MedicineRequestService(MedicineRequestRepository requests,
                                  MedicineRequestStatusHistoryRepository history,
                                  PatientProfileRepository patients, UserRepository users,
                                  CityRepository cities, AreaRepository areas,
                                  PrescriptionRepository prescriptions, JdbcClient jdbc,
                                  MedicineRequestProperties properties, AuditService audit) {
        this.requests = requests; this.history = history; this.patients = patients; this.users = users;
        this.cities = cities; this.areas = areas; this.prescriptions = prescriptions; this.jdbc = jdbc;
        this.properties = properties; this.audit = audit;
    }

    @Transactional
    public MedicineRequestResponse create(CreateMedicineRequest body, String idempotencyKey,
                                          UUID userId, RequestMetadata metadata) {
        requireQuantity(body.quantity());
        PackageEligibility medicine = packageEligibility(body.medicinePackageId());
        requireRequestable(medicine);
        String hash = hash(body.toString());
        UUID replay = reserve(userId, "CREATE", idempotencyKey, hash);
        if (replay != null) {
            log.info("Medicine request create replayed requestId={} patientUserId={}", replay, userId);
            return response(owned(replay, userId), true);
        }
        var patient = patients.findByUserId(userId).orElseThrow(() ->
                new ApiException(HttpStatus.FORBIDDEN, "PATIENT_PROFILE_REQUIRED", "Complete the patient profile first."));
        var city = cities.findByIdAndActiveTrue(body.cityId()).orElseThrow(() ->
                new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CITY", "Select a supported city."));
        var area = areas.findByIdAndCityIdAndActiveTrue(body.areaId(), city.getId()).orElseThrow(() ->
                new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AREA", "Select an area in the chosen city."));
        var request = new MedicineRequest(reference(), patient, city, area, body.fulfillmentPreference(), body.urgency(),
                properties.initialRadiusKm(), body.medicinePackageId(), body.quantity(), clean(body.notes()));
        if (body.prescriptionId() != null) request.attachPrescription(ownedPrescription(body.prescriptionId(), userId));
        // The idempotency row references this request through JDBC below, so the JPA insert must
        // reach the database first instead of waiting for transaction commit.
        requests.saveAndFlush(request);
        history.save(new MedicineRequestStatusHistory(request, null, MedicineRequestStatus.DRAFT,
                patient.getUser(), "Request created", Instant.now()));
        completeReservation(userId, "CREATE", idempotencyKey, request.getId());
        audit.record(patient.getUser(), "MEDICINE_REQUEST_CREATED", "MEDICINE_REQUEST", request.getId(),
                "SUCCESS", metadata, "status=DRAFT");
        log.info("Medicine request created requestId={} reference={} patientUserId={} packageId={} quantity={} status=DRAFT",
                request.getId(), request.getReferenceNumber(), userId, body.medicinePackageId(), body.quantity());
        return response(request, false);
    }

    @Transactional
    public MedicineRequestResponse submit(UUID id, String idempotencyKey, UUID userId, RequestMetadata metadata) {
        String hash = hash(id.toString());
        UUID replay = reserve(userId, "SUBMIT", idempotencyKey, hash);
        if (replay != null) return response(owned(replay, userId), true);
        MedicineRequest request = ownedForUpdate(id, userId);
        User actor = user(userId);
        log.info("Medicine request submission started requestId={} patientUserId={} status={}", id, userId, request.getStatus());
        transition(request, MedicineRequestStatus.SUBMITTED, actor, "Patient submitted request");
        request.setExpiry(Instant.now().plus(properties.ttl()));
        qualifyAndMatch(request, actor);
        completeReservation(userId, "SUBMIT", idempotencyKey, request.getId());
        audit.record(actor, "MEDICINE_REQUEST_SUBMITTED", "MEDICINE_REQUEST", id, "SUCCESS", metadata,
                "status=" + request.getStatus());
        log.info("Medicine request submission completed requestId={} patientUserId={} status={} matchedBranches={}",
                id, userId, request.getStatus(), matchCount(id));
        return response(request, false);
    }

    @Transactional
    public MedicineRequestResponse qualify(UUID id, QualifyMedicineRequest body, String idempotencyKey,
                                           UUID userId, RequestMetadata metadata) {
        String hash = hash(id + ":" + body.prescriptionId());
        UUID replay = reserve(userId, "QUALIFY", idempotencyKey, hash);
        if (replay != null) return response(owned(replay, userId), true);
        MedicineRequest request = ownedForUpdate(id, userId);
        if (request.getStatus() != MedicineRequestStatus.AWAITING_PRESCRIPTION
                && request.getStatus() != MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW) {
            throw conflict("REQUEST_CANNOT_BE_QUALIFIED", "Request is not awaiting prescription qualification.");
        }
        if (body.prescriptionId() != null) request.attachPrescription(ownedPrescription(body.prescriptionId(), userId));
        qualifyAndMatch(request, user(userId));
        completeReservation(userId, "QUALIFY", idempotencyKey, id);
        audit.record(user(userId), "MEDICINE_REQUEST_QUALIFIED", "MEDICINE_REQUEST", id, "SUCCESS", metadata,
                "status=" + request.getStatus());
        log.info("Medicine request qualification completed requestId={} patientUserId={} status={} matchedBranches={}",
                id, userId, request.getStatus(), matchCount(id));
        return response(request, false);
    }

    @Transactional
    public MedicineRequestResponse expandRadius(UUID id, ExpandRadiusRequest body, String idempotencyKey,
                                                UUID userId, RequestMetadata metadata) {
        if (body.radiusKm() > properties.maxRadiusKm()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REQUEST_RADIUS_TOO_LARGE",
                    "Search radius exceeds the configured maximum.");
        }
        String hash = hash(id + ":" + body.radiusKm());
        UUID replay = reserve(userId, "EXPAND_RADIUS", idempotencyKey, hash);
        if (replay != null) return response(owned(replay, userId), true);
        MedicineRequest request = ownedForUpdate(id, userId);
        if (request.getStatus() != MedicineRequestStatus.UNFULFILLED || body.radiusKm() <= request.getSearchRadiusKm()) {
            throw conflict("REQUEST_RADIUS_CANNOT_BE_EXPANDED", "Only an unfulfilled request can expand to a larger radius.");
        }
        request.expandRadius(body.radiusKm());
        transition(request, MedicineRequestStatus.MATCHING, user(userId), "Patient expanded matching radius");
        int matched = createMatches(request);
        if (matched == 0) transition(request, MedicineRequestStatus.UNFULFILLED, user(userId), "No additional eligible branches");
        completeReservation(userId, "EXPAND_RADIUS", idempotencyKey, id);
        audit.record(user(userId), "MEDICINE_REQUEST_MATCHING_EXPANDED", "MEDICINE_REQUEST", id,
                "SUCCESS", metadata, "radiusKm=" + body.radiusKm() + ";newMatches=" + matched);
        log.info("Medicine request radius expanded requestId={} patientUserId={} radiusKm={} newMatches={} status={}",
                id, userId, body.radiusKm(), matched, request.getStatus());
        return response(request, false);
    }

    @Transactional
    public MedicineRequestResponse cancel(UUID id, CancelMedicineRequest body, String idempotencyKey,
                                          UUID userId, RequestMetadata metadata) {
        String hash = hash(id + ":" + body.reasonCode() + ":" + body.reasonText());
        UUID replay = reserve(userId, "CANCEL", idempotencyKey, hash);
        if (replay != null) return response(owned(replay, userId), true);
        MedicineRequest request = ownedForUpdate(id, userId);
        MedicineRequestStatus old;
        try { old = request.cancel(user(userId), body.reasonCode(), clean(body.reasonText()), Instant.now()); }
        catch (IllegalStateException exception) { throw conflict("INVALID_REQUEST_TRANSITION", exception.getMessage()); }
        recordHistory(request, old, MedicineRequestStatus.CANCELLED, user(userId), "Patient cancelled request");
        completeReservation(userId, "CANCEL", idempotencyKey, id);
        audit.record(user(userId), "MEDICINE_REQUEST_CANCELLED", "MEDICINE_REQUEST", id, "SUCCESS", metadata,
                "reasonCode=" + body.reasonCode());
        log.info("Medicine request cancelled requestId={} patientUserId={} oldStatus={} reasonCode={}",
                id, userId, old, body.reasonCode());
        return response(request, false);
    }

    @Transactional(readOnly = true)
    public MedicineRequestResponse get(UUID id, UUID userId) { return response(owned(id, userId), false); }

    @Transactional(readOnly = true)
    public List<MedicineRequestResponse> list(UUID userId) {
        return requests.findByPatientProfileUserIdOrderByCreatedAtDesc(userId).stream()
                .map(value -> response(value, false)).toList();
    }

    @Scheduled(cron = "${dawak.request.expiration-cleanup-cron}")
    @Transactional
    public void expireDueRequests() {
        Instant cutoff = Instant.now();
        var expired = requests.findExpiredForUpdate(cutoff, EXPIRABLE_STATUSES);
        for (MedicineRequest request : expired) {
            transition(request, MedicineRequestStatus.EXPIRED, null, "Request validity period elapsed");
            audit.record(null, "MEDICINE_REQUEST_EXPIRED", "MEDICINE_REQUEST", request.getId(),
                    "SUCCESS", SYSTEM_METADATA, "expiresAt=" + request.getExpiresAt());
        }
        if (!expired.isEmpty()) {
            log.info("Medicine request expiration cleanup completed expiredRequests={}", expired.size());
        }
    }

    private void qualifyAndMatch(MedicineRequest request, User actor) {
        PackageEligibility medicine = packageEligibility(request.getItem().getMedicinePackageId());
        requireRequestable(medicine);
        requireQuantity(request.getItem().getRequestedQuantity());
        if (medicine.prescriptionRequired()) {
            Prescription prescription = request.getPrescription();
            if (prescription == null || prescription.getStatus() == PrescriptionStatus.NEEDS_CLARIFICATION
                    || prescription.getStatus() == PrescriptionStatus.REJECTED
                    || prescription.getStatus() == PrescriptionStatus.SCAN_FAILED
                    || prescription.getStatus() == PrescriptionStatus.EXPIRED
                    || prescription.getStatus() == PrescriptionStatus.DELETED) {
                moveToAwaitingPrescription(request, actor);
                return;
            }
            requirePrescriptionAssociation(request, prescription);
            if (prescription.getStatus() != PrescriptionStatus.APPROVED) {
                moveToPendingReview(request, actor);
                return;
            }
            if (prescription.getValidUntil() != null && !prescription.getValidUntil().isAfter(Instant.now())) {
                moveToAwaitingPrescription(request, actor);
                return;
            }
        }
        moveToReady(request, actor);
        transition(request, MedicineRequestStatus.MATCHING, actor, "Qualification passed; matching started");
        int matched = createMatches(request);
        if (matched == 0) transition(request, MedicineRequestStatus.UNFULFILLED, actor, "No eligible branches found");
    }

    private void moveToAwaitingPrescription(MedicineRequest request, User actor) {
        if (request.getStatus() == MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW) {
            transition(request, MedicineRequestStatus.AWAITING_PRESCRIPTION, actor, "Prescription must be replaced");
        } else if (request.getStatus() == MedicineRequestStatus.SUBMITTED) {
            transition(request, MedicineRequestStatus.AWAITING_PRESCRIPTION, actor, "Approved prescription required");
        }
    }

    private void moveToPendingReview(MedicineRequest request, User actor) {
        if (request.getStatus() == MedicineRequestStatus.SUBMITTED
                || request.getStatus() == MedicineRequestStatus.AWAITING_PRESCRIPTION) {
            transition(request, MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW, actor, "Prescription review pending");
        }
    }

    private void moveToReady(MedicineRequest request, User actor) {
        if (request.getStatus() == MedicineRequestStatus.AWAITING_PRESCRIPTION) {
            transition(request, MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW, actor, "Prescription attached");
        }
        transition(request, MedicineRequestStatus.READY_FOR_MATCHING, actor, "Request qualification passed");
    }

    private int createMatches(MedicineRequest request) {
        boolean prescriptionRequired = packageEligibility(request.getItem().getMedicinePackageId()).prescriptionRequired();
        List<BranchCandidate> candidates = jdbc.sql("""
                select b.id, sa.distance_km
                  from pharmacy_branch b
                  join pharmacy p on p.id=b.pharmacy_id
                  join pharmacy_branch_service_area sa on sa.pharmacy_branch_id=b.id and sa.area_id=:area_id
                 where p.status='APPROVED' and p.license_expiry_date >= current_date
                   and b.status='ACTIVE' and b.accepting_requests=true and b.city_id=:city_id
                   and sa.distance_km <= :radius
                   and (:prescription_required=false or b.prescription_handling_enabled=true)
                   and (:fulfillment='EITHER'
                        or (:fulfillment='PICKUP' and b.pickup_enabled=true)
                        or (:fulfillment='DELIVERY' and b.delivery_enabled=true))
                   and not exists (select 1 from request_pharmacy_match rpm
                                    where rpm.medicine_request_id=:request_id and rpm.pharmacy_branch_id=b.id)
                 order by sa.distance_km, b.created_at
                 limit :batch_size
                """).param("area_id", request.getArea().getId()).param("city_id", request.getCity().getId())
                .param("radius", request.getSearchRadiusKm()).param("prescription_required", prescriptionRequired)
                .param("fulfillment", request.getFulfillmentPreference().name()).param("request_id", request.getId())
                .param("batch_size", properties.matchingBatchSize())
                .query((rs, row) -> new BranchCandidate(rs.getObject(1, UUID.class), rs.getBigDecimal(2))).list();
        int inserted = 0;
        for (BranchCandidate candidate : candidates) {
            inserted += jdbc.sql("""
                    insert into request_pharmacy_match(id,medicine_request_id,pharmacy_branch_id,distance_km,
                      match_score,match_reason,notification_status,created_at)
                    values (:id,:request_id,:branch_id,:distance,:score,'ELIGIBILITY_RULES_PASSED','PENDING',now())
                    on conflict(medicine_request_id,pharmacy_branch_id) do nothing
                    """).param("id", UUID.randomUUID()).param("request_id", request.getId())
                    .param("branch_id", candidate.id()).param("distance", candidate.distanceKm())
                    .param("score", java.math.BigDecimal.valueOf(100).subtract(candidate.distanceKm())).update();
        }
        log.info("Medicine request matching evaluated requestId={} radiusKm={} candidates={} insertedMatches={}",
                request.getId(), request.getSearchRadiusKm(), candidates.size(), inserted);
        return inserted;
    }

    private void requirePrescriptionAssociation(MedicineRequest request, Prescription prescription) {
        if (!prescription.getPatientProfile().getId().equals(request.getPatientProfile().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PRESCRIPTION_PATIENT_MISMATCH", "Prescription does not belong to this patient.");
        }
        if (prescription.getMedicinePackageId() == null
                || !prescription.getMedicinePackageId().equals(request.getItem().getMedicinePackageId())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PRESCRIPTION_MEDICINE_MISMATCH",
                    "Prescription was not qualified for the selected medicine package.");
        }
    }

    private Prescription ownedPrescription(UUID id, UUID userId) {
        Prescription value = prescriptions.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found."));
        if (!value.getPatientProfile().getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRESCRIPTION_NOT_FOUND", "Prescription was not found.");
        }
        return value;
    }

    private PackageEligibility packageEligibility(UUID id) {
        return jdbc.sql("""
                select mp.active package_active, mp.status, m.active medicine_active,
                       m.restricted, m.prescription_required
                  from medicine_package mp join medicine m on m.id=mp.medicine_id where mp.id=:id
                """).param("id", id).query((rs, row) -> new PackageEligibility(rs.getBoolean(1),
                        rs.getString(2), rs.getBoolean(3), rs.getBoolean(4), rs.getBoolean(5))).optional()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_PACKAGE_NOT_FOUND", "Medicine package not found."));
    }

    private void requireRequestable(PackageEligibility value) {
        if (value.restricted()) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RESTRICTED_MEDICINE",
                "Restricted medicines cannot use the standard request workflow.");
        if (!value.packageActive() || !value.medicineActive() || !"AVAILABLE".equals(value.status())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MEDICINE_PACKAGE_NOT_REQUESTABLE",
                    "The selected medicine package is not currently requestable.");
        }
    }

    private void requireQuantity(int quantity) {
        if (quantity < 1 || quantity > properties.maxQuantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_QUANTITY",
                    "Quantity must be between 1 and " + properties.maxQuantity() + ".");
        }
    }

    private void transition(MedicineRequest request, MedicineRequestStatus target, User actor, String reason) {
        MedicineRequestStatus old;
        try { old = request.transition(target, Instant.now()); }
        catch (IllegalStateException exception) { throw conflict("INVALID_REQUEST_TRANSITION", exception.getMessage()); }
        recordHistory(request, old, target, actor, reason);
        log.info("Medicine request state changed requestId={} oldStatus={} newStatus={} reason={}",
                request.getId(), old, target, reason);
    }

    private void recordHistory(MedicineRequest request, MedicineRequestStatus old, MedicineRequestStatus target,
                               User actor, String reason) {
        history.save(new MedicineRequestStatusHistory(request, old, target, actor, reason, Instant.now()));
    }

    private UUID reserve(UUID userId, String operation, String key, String requestHash) {
        if (key == null || key.isBlank() || key.length() > 120) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED",
                    "A valid Idempotency-Key header is required.");
        }
        jdbc.sql("""
                insert into medicine_request_idempotency_key(user_id,operation,idempotency_key,request_hash,created_at)
                values (:user_id,:operation,:key,:hash,now()) on conflict do nothing
                """).param("user_id", userId).param("operation", operation).param("key", key).param("hash", requestHash).update();
        Idempotency row = jdbc.sql("""
                select request_hash,medicine_request_id from medicine_request_idempotency_key
                 where user_id=:user_id and operation=:operation and idempotency_key=:key for update
                """).param("user_id", userId).param("operation", operation).param("key", key)
                .query((rs, n) -> new Idempotency(rs.getString(1), rs.getObject(2, UUID.class))).single();
        if (!row.hash().equals(requestHash)) throw conflict("IDEMPOTENCY_KEY_REUSED",
                "Idempotency key was already used with a different request.");
        return row.requestId();
    }

    private void completeReservation(UUID userId, String operation, String key, UUID requestId) {
        jdbc.sql("""
                update medicine_request_idempotency_key set medicine_request_id=:request_id
                 where user_id=:user_id and operation=:operation and idempotency_key=:key
                """).param("request_id", requestId).param("user_id", userId).param("operation", operation)
                .param("key", key).update();
    }

    private MedicineRequest owned(UUID id, UUID userId) { return requests.findByIdAndPatientProfileUserId(id, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_REQUEST_NOT_FOUND", "Medicine request not found.")); }
    private MedicineRequest ownedForUpdate(UUID id, UUID userId) { return requests.findOwnedByIdForUpdate(id, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_REQUEST_NOT_FOUND", "Medicine request not found.")); }
    private User user(UUID id) { return users.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.UNAUTHORIZED, "ACCOUNT_UNAVAILABLE", "Account unavailable.")); }
    private MedicineRequestResponse response(MedicineRequest request, boolean replayed) {
        return MedicineRequestResponse.from(request, matchCount(request.getId()), replayed);
    }
    private int matchCount(UUID id) { return jdbc.sql("select count(*) from request_pharmacy_match where medicine_request_id=:id")
            .param("id", id).query(Long.class).single().intValue(); }
    private String reference() { return "REQ-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim().replaceAll("\\s+", " "); }
    private ApiException conflict(String code, String message) { return new ApiException(HttpStatus.CONFLICT, code, message); }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private record PackageEligibility(boolean packageActive, String status, boolean medicineActive,
                                      boolean restricted, boolean prescriptionRequired) {}
    private record BranchCandidate(UUID id, java.math.BigDecimal distanceKm) {}
    private record Idempotency(String hash, UUID requestId) {}
}
