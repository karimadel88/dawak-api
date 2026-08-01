package com.dawak.api.identity.domain;

import com.dawak.api.common.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
public class AuthSession extends MutableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Column(name = "device_id", nullable = false, length = 200)
    private String deviceId;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected AuthSession() {
    }

    public AuthSession(User user, String refreshTokenHash, String deviceId, String deviceName,
                       String ipAddress, String userAgent, Instant now, Instant expiresAt) {
        super(UUID.randomUUID());
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.lastUsedAt = now;
        this.expiresAt = expiresAt;
    }

    public void rotate(String newHash, Instant at) {
        this.refreshTokenHash = newHash;
        this.lastUsedAt = at;
    }

    public void revoke(Instant at) { this.revokedAt = at; }
    public boolean isUsable(Instant now) { return revokedAt == null && expiresAt.isAfter(now); }
    public User getUser() { return user; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
