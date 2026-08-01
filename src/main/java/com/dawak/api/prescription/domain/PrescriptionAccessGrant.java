package com.dawak.api.prescription.domain;

import com.dawak.api.identity.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription_access_grant")
public class PrescriptionAccessGrant {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "prescription_id")
    private Prescription prescription;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(nullable = false, length = 80) private String purpose;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected PrescriptionAccessGrant() {}
    public PrescriptionAccessGrant(Prescription prescription, User user, String tokenHash, String purpose,
                                   Instant expiresAt, Instant now) {
        this.id = UUID.randomUUID(); this.prescription = prescription; this.user = user;
        this.tokenHash = tokenHash; this.purpose = purpose; this.expiresAt = expiresAt; this.createdAt = now;
    }
    public void use(Instant now) { this.usedAt = now; }
    public Prescription getPrescription() { return prescription; }
    public User getUser() { return user; }
    public String getPurpose() { return purpose; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
}
