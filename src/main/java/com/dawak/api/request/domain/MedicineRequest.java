package com.dawak.api.request.domain;

import com.dawak.api.common.persistence.MutableEntity;
import com.dawak.api.identity.domain.User;
import com.dawak.api.patient.domain.Area;
import com.dawak.api.patient.domain.City;
import com.dawak.api.patient.domain.PatientProfile;
import com.dawak.api.prescription.domain.Prescription;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "medicine_request")
public class MedicineRequest extends MutableEntity {
    private static final Map<MedicineRequestStatus, EnumSet<MedicineRequestStatus>> ALLOWED = Map.ofEntries(
            Map.entry(MedicineRequestStatus.DRAFT, EnumSet.of(MedicineRequestStatus.SUBMITTED, MedicineRequestStatus.CANCELLED)),
            Map.entry(MedicineRequestStatus.SUBMITTED, EnumSet.of(MedicineRequestStatus.AWAITING_PRESCRIPTION,
                    MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW, MedicineRequestStatus.READY_FOR_MATCHING,
                    MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.AWAITING_PRESCRIPTION, EnumSet.of(MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW,
                    MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.PENDING_PRESCRIPTION_REVIEW, EnumSet.of(MedicineRequestStatus.READY_FOR_MATCHING,
                    MedicineRequestStatus.AWAITING_PRESCRIPTION, MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.READY_FOR_MATCHING, EnumSet.of(MedicineRequestStatus.MATCHING,
                    MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.MATCHING, EnumSet.of(MedicineRequestStatus.OFFERS_AVAILABLE,
                    MedicineRequestStatus.UNFULFILLED, MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.OFFERS_AVAILABLE, EnumSet.of(MedicineRequestStatus.OFFER_SELECTED,
                    MedicineRequestStatus.UNFULFILLED, MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED)),
            Map.entry(MedicineRequestStatus.OFFER_SELECTED, EnumSet.of(MedicineRequestStatus.COMPLETED, MedicineRequestStatus.CANCELLED)),
            Map.entry(MedicineRequestStatus.UNFULFILLED, EnumSet.of(MedicineRequestStatus.MATCHING,
                    MedicineRequestStatus.CANCELLED, MedicineRequestStatus.EXPIRED))
    );

    @Column(name = "reference_number", nullable = false, unique = true, length = 40) private String referenceNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "patient_profile_id") private PatientProfile patientProfile;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "prescription_id") private Prescription prescription;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "city_id") private City city;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "area_id") private Area area;
    @Enumerated(EnumType.STRING) @Column(name = "fulfillment_preference", nullable = false, length = 20)
    private FulfillmentPreference fulfillmentPreference;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RequestUrgency urgency;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private MedicineRequestStatus status;
    @Column(name = "search_radius_km", nullable = false) private int searchRadiusKm;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "matching_started_at") private Instant matchingStartedAt;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "cancel_reason_code", length = 80) private String cancelReasonCode;
    @Column(name = "cancel_reason_text", length = 500) private String cancelReasonText;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cancelled_by_user_id") private User cancelledBy;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MedicineRequestItem item;

    protected MedicineRequest() {}
    public MedicineRequest(String referenceNumber, PatientProfile patient, City city, Area area,
                           FulfillmentPreference fulfillmentPreference, RequestUrgency urgency,
                           int radiusKm, UUID medicinePackageId, int quantity, String notes) {
        super(UUID.randomUUID()); this.referenceNumber = referenceNumber; this.patientProfile = patient;
        this.city = city; this.area = area; this.fulfillmentPreference = fulfillmentPreference;
        this.urgency = urgency; this.searchRadiusKm = radiusKm; this.status = MedicineRequestStatus.DRAFT;
        this.item = new MedicineRequestItem(this, medicinePackageId, quantity, notes);
    }

    public MedicineRequestStatus transition(MedicineRequestStatus target, Instant now) {
        var allowed = ALLOWED.get(status);
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalStateException("Transition " + status + " -> " + target + " is not allowed");
        }
        MedicineRequestStatus previous = status;
        status = target;
        if (target == MedicineRequestStatus.SUBMITTED) submittedAt = now;
        if (target == MedicineRequestStatus.MATCHING) matchingStartedAt = now;
        return previous;
    }

    public void setExpiry(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void attachPrescription(Prescription prescription) { this.prescription = prescription; }
    public void expandRadius(int radiusKm) { this.searchRadiusKm = radiusKm; }
    public MedicineRequestStatus cancel(User actor, String code, String text, Instant now) {
        MedicineRequestStatus previous = transition(MedicineRequestStatus.CANCELLED, now);
        this.cancelledBy = actor; this.cancelReasonCode = code; this.cancelReasonText = text; this.cancelledAt = now;
        return previous;
    }

    public String getReferenceNumber() { return referenceNumber; }
    public PatientProfile getPatientProfile() { return patientProfile; }
    public Prescription getPrescription() { return prescription; }
    public City getCity() { return city; }
    public Area getArea() { return area; }
    public FulfillmentPreference getFulfillmentPreference() { return fulfillmentPreference; }
    public RequestUrgency getUrgency() { return urgency; }
    public MedicineRequestStatus getStatus() { return status; }
    public int getSearchRadiusKm() { return searchRadiusKm; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getMatchingStartedAt() { return matchingStartedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public MedicineRequestItem getItem() { return item; }
}
