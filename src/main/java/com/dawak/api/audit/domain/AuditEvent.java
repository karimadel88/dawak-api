package com.dawak.api.audit.domain;

import com.dawak.api.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEvent {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_user_id") private User actor;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @Column(name = "aggregate_type", nullable = false, length = 80) private String aggregateType;
    @Column(name = "aggregate_id") private UUID aggregateId;
    @Column(nullable = false, length = 30) private String outcome;
    @Column(name = "correlation_id", length = 100) private String correlationId;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "user_agent", length = 500) private String userAgent;
    @Column private String metadata;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AuditEvent() {}

    public AuditEvent(User actor, String eventType, String aggregateType, UUID aggregateId,
                      String outcome, String ipAddress, String userAgent, String metadata, Instant at) {
        this.id = UUID.randomUUID();
        this.actor = actor;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.outcome = outcome;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.metadata = metadata;
        this.createdAt = at;
    }

    public UUID getId() { return id; }
}
