package com.dawak.api.identity.domain;

import com.dawak.api.common.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User extends MutableEntity {
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "phone_number_verified_at", nullable = false)
    private Instant phoneNumberVerifiedAt;

    @Column(length = 254)
    private String email;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserStatusV1 status;

    @Column(name = "preferred_language", nullable = false, length = 5)
    private String preferredLanguage;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected User() {
    }

    public User(String phoneNumber, Instant verifiedAt) {
        super(UUID.randomUUID());
        this.phoneNumber = phoneNumber;
        this.phoneNumberVerifiedAt = verifiedAt;
        this.status = UserStatusV1.PENDING_VERIFICATION;
        this.preferredLanguage = "ar";
        this.lastLoginAt = verifiedAt;
    }

    public void recordLogin(Instant at) { this.lastLoginAt = at; }

    public void completeRegistration(String email, String preferredLanguage) {
        this.email = email;
        this.preferredLanguage = preferredLanguage;
        this.status = UserStatusV1.ACTIVE;
    }

    public void updateContact(String email, String preferredLanguage) {
        this.email = email;
        this.preferredLanguage = preferredLanguage;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public Instant getPhoneNumberVerifiedAt() { return phoneNumberVerifiedAt; }
    public String getEmail() { return email; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public UserStatusV1 getStatus() { return status; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public Instant getLastLoginAt() { return lastLoginAt; }
}
