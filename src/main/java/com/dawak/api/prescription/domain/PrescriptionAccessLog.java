package com.dawak.api.prescription.domain;

import com.dawak.api.identity.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription_access_log")
public class PrescriptionAccessLog {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "prescription_id") private Prescription prescription;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "accessed_by_user_id") private User user;
    @Column(name = "access_type", nullable = false, length = 40) private String accessType;
    @Column(nullable = false, length = 80) private String purpose;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "user_agent", length = 500) private String userAgent;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected PrescriptionAccessLog() {}
    public PrescriptionAccessLog(Prescription prescription, User user, String accessType, String purpose,
                                 String ipAddress, String userAgent, Instant now) {
        this.id = UUID.randomUUID(); this.prescription = prescription; this.user = user;
        this.accessType = accessType; this.purpose = purpose; this.ipAddress = ipAddress;
        this.userAgent = userAgent; this.createdAt = now;
    }
}
