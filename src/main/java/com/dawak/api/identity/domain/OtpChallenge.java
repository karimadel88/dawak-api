package com.dawak.api.identity.domain;

import com.dawak.api.common.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_challenge")
public class OtpChallenge extends MutableEntity {
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "request_ip", length = 64)
    private String requestIp;

    protected OtpChallenge() {
    }

    public OtpChallenge(String phoneNumber, String codeHash, Instant expiresAt, int maxAttempts, String requestIp) {
        super(UUID.randomUUID());
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.maxAttempts = maxAttempts;
        this.requestIp = requestIp;
    }

    public void recordFailedAttempt() { this.attemptCount++; }
    public void consume(Instant at) { this.consumedAt = at; }
    public boolean isConsumed() { return consumedAt != null; }
    public boolean isExpired(Instant now) { return !expiresAt.isAfter(now); }
    public boolean attemptsExhausted() { return attemptCount >= maxAttempts; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCodeHash() { return codeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getAttemptCount() { return attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getConsumedAt() { return consumedAt; }
}
