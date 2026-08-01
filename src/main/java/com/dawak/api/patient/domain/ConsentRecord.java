package com.dawak.api.patient.domain;

import com.dawak.api.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_record")
public class ConsentRecord {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 60)
    private ConsentTypeV1 consentType;
    @Column(name = "document_version", nullable = false, length = 40)
    private String documentVersion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsentStatusV1 status;
    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;
    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;
    @Column(nullable = false, length = 40)
    private String source;
    @Column(name = "ip_address", length = 64)
    private String ipAddress;
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ConsentRecord() {}

    public ConsentRecord(User user, ConsentTypeV1 type, String documentVersion, String source,
                         String ipAddress, String userAgent, Instant at) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.consentType = type;
        this.documentVersion = documentVersion;
        this.status = ConsentStatusV1.GRANTED;
        this.grantedAt = at;
        this.source = source;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = at;
    }

    public UUID getId() { return id; }
    public ConsentTypeV1 getConsentType() { return consentType; }
    public String getDocumentVersion() { return documentVersion; }
    public ConsentStatusV1 getStatus() { return status; }
    public Instant getGrantedAt() { return grantedAt; }
}
