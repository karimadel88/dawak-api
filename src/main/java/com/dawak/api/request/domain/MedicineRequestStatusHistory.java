package com.dawak.api.request.domain;

import com.dawak.api.identity.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicine_request_status_history")
public class MedicineRequestStatusHistory {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicine_request_id") private MedicineRequest request;
    @Enumerated(EnumType.STRING) @Column(name = "old_status", length = 50) private MedicineRequestStatus oldStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, length = 50) private MedicineRequestStatus newStatus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_user_id") private User actor;
    @Column(length = 500) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected MedicineRequestStatusHistory() {}
    public MedicineRequestStatusHistory(MedicineRequest request, MedicineRequestStatus oldStatus,
                                        MedicineRequestStatus newStatus, User actor, String reason, Instant now) {
        this.id = UUID.randomUUID(); this.request = request; this.oldStatus = oldStatus;
        this.newStatus = newStatus; this.actor = actor; this.reason = reason; this.createdAt = now;
    }
}
