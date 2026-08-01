package com.dawak.api.identity.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.api.dto.OtpRequestResponse;
import com.dawak.api.identity.api.dto.OtpVerificationRequest;
import com.dawak.api.identity.api.dto.TokenResponse;
import com.dawak.api.identity.config.AuthProperties;
import com.dawak.api.identity.domain.AuthSession;
import com.dawak.api.identity.domain.OtpChallenge;
import com.dawak.api.identity.domain.User;
import com.dawak.api.identity.domain.UserStatusV1;
import com.dawak.api.identity.persistence.AuthSessionRepository;
import com.dawak.api.identity.persistence.OtpChallengeRepository;
import com.dawak.api.identity.persistence.UserRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class OtpAuthenticationService {
    private final AuthProperties properties;
    private final PhoneNumberNormalizer phoneNumbers;
    private final OtpHashService otpHashes;
    private final OtpDeliveryPort delivery;
    private final OtpChallengeRepository challenges;
    private final UserRepository users;
    private final AuthSessionRepository sessions;
    private final PatientProfileRepository profiles;
    private final TokenService tokens;
    private final AuditService audit;
    private final SecureRandom random = new SecureRandom();

    public OtpAuthenticationService(AuthProperties properties, PhoneNumberNormalizer phoneNumbers,
                                    OtpHashService otpHashes, OtpDeliveryPort delivery,
                                    OtpChallengeRepository challenges, UserRepository users,
                                    AuthSessionRepository sessions, PatientProfileRepository profiles,
                                    TokenService tokens, AuditService audit) {
        this.properties = properties;
        this.phoneNumbers = phoneNumbers;
        this.otpHashes = otpHashes;
        this.delivery = delivery;
        this.challenges = challenges;
        this.users = users;
        this.sessions = sessions;
        this.profiles = profiles;
        this.tokens = tokens;
        this.audit = audit;
    }

    @Transactional
    public OtpRequestResponse request(String rawPhoneNumber, RequestMetadata metadata) {
        String phone = phoneNumbers.normalizeEgyptian(rawPhoneNumber);
        Instant now = Instant.now();
        long recent = challenges.countByPhoneNumberAndCreatedAtAfter(phone, now.minus(properties.otpRequestWindow()));
        if (recent >= properties.otpRequestLimit()) {
            audit.record(null, "OTP_REQUEST_RATE_LIMITED", "PHONE", null, "REJECTED", metadata, "phone=masked");
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_RATE_LIMITED",
                    "Too many verification requests. Try again later.");
        }

        String code = "%06d".formatted(random.nextInt(1_000_000));
        var challenge = challenges.save(new OtpChallenge(phone, otpHashes.hash(phone, code),
                now.plus(properties.otpTtl()), properties.otpMaxAttempts(), metadata.ipAddress()));
        delivery.deliver(phone, code, properties.otpTtl().toSeconds());
        audit.record(null, "OTP_REQUESTED", "OTP_CHALLENGE", challenge.getId(), "SUCCESS", metadata, "phone=masked");
        return new OtpRequestResponse(challenge.getId(), properties.otpTtl().toSeconds(), 60);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse verify(OtpVerificationRequest request, RequestMetadata metadata) {
        String phone = phoneNumbers.normalizeEgyptian(request.phoneNumber());
        Instant now = Instant.now();
        OtpChallenge challenge = challenges.findByIdForUpdate(request.challengeId())
                .orElseThrow(this::invalidOtp);

        if (!challenge.getPhoneNumber().equals(phone)) throw invalidOtp();
        if (challenge.isConsumed()) throw new ApiException(HttpStatus.UNAUTHORIZED, "OTP_REPLAYED", "The verification code is no longer valid.");
        if (challenge.isExpired(now)) throw new ApiException(HttpStatus.UNAUTHORIZED, "OTP_EXPIRED", "The verification code has expired.");
        if (challenge.attemptsExhausted()) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_ATTEMPTS_EXCEEDED", "Too many invalid verification attempts.");

        String suppliedHash = otpHashes.hash(phone, request.code());
        if (!otpHashes.matches(challenge.getCodeHash(), suppliedHash)) {
            challenge.recordFailedAttempt();
            challenges.save(challenge);
            audit.record(null, "OTP_VERIFICATION_FAILED", "OTP_CHALLENGE", challenge.getId(), "REJECTED", metadata,
                    "attempt=" + challenge.getAttemptCount());
            if (challenge.attemptsExhausted()) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_ATTEMPTS_EXCEEDED", "Too many invalid verification attempts.");
            }
            throw invalidOtp();
        }

        challenge.consume(now);
        User user = users.findByPhoneNumber(phone).orElseGet(() -> users.save(new User(phone, now)));
        rejectUnavailableAccount(user);
        user.recordLogin(now);

        sessions.findByUserIdAndDeviceIdAndRevokedAtIsNull(user.getId(), request.deviceId())
                .ifPresent(existing -> {
                    existing.revoke(now);
                    sessions.saveAndFlush(existing);
                });
        String refreshToken = tokens.newRefreshToken();
        var session = sessions.save(new AuthSession(user, tokens.hash(refreshToken), request.deviceId(),
                request.deviceName(), metadata.ipAddress(), metadata.userAgent(), now,
                now.plus(properties.refreshTokenTtl())));
        audit.record(user, "OTP_VERIFIED", "AUTH_SESSION", session.getId(), "SUCCESS", metadata, null);
        return response(user, session, refreshToken, now);
    }

    private TokenResponse response(User user, AuthSession session, String refreshToken, Instant now) {
        return new TokenResponse("Bearer", tokens.accessToken(user, session, now),
                tokens.accessTokenExpiresInSeconds(), refreshToken, session.getId(),
                !profiles.existsByUserId(user.getId()));
    }

    private void rejectUnavailableAccount(User user) {
        if (user.getStatus() == UserStatusV1.LOCKED || user.getStatus() == UserStatusV1.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "This account cannot sign in. Contact support.");
        }
        if (user.getStatus() == UserStatusV1.DEACTIVATED || user.getStatus() == UserStatusV1.DELETED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_UNAVAILABLE", "This account cannot sign in. Contact support.");
        }
    }

    private ApiException invalidOtp() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_OTP", "The verification code is invalid.");
    }
}
