package com.dawak.api.identity.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.api.dto.SessionResponse;
import com.dawak.api.identity.api.dto.TokenResponse;
import com.dawak.api.identity.domain.AuthSession;
import com.dawak.api.identity.domain.UserStatusV1;
import com.dawak.api.identity.persistence.AuthSessionRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {
    private final AuthSessionRepository sessions;
    private final TokenService tokens;
    private final PatientProfileRepository profiles;
    private final AuditService audit;

    public SessionService(AuthSessionRepository sessions, TokenService tokens,
                          PatientProfileRepository profiles, AuditService audit) {
        this.sessions = sessions;
        this.tokens = tokens;
        this.profiles = profiles;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refresh(String refreshToken, RequestMetadata metadata) {
        Instant now = Instant.now();
        AuthSession session = sessions.findByRefreshTokenHash(tokens.hash(refreshToken))
                .orElseThrow(this::invalidRefresh);
        if (!session.isUsable(now)) throw invalidRefresh();
        if (session.getUser().getStatus() != UserStatusV1.ACTIVE
                && session.getUser().getStatus() != UserStatusV1.PENDING_VERIFICATION) {
            session.revoke(now);
            throw invalidRefresh();
        }
        String rotated = tokens.newRefreshToken();
        session.rotate(tokens.hash(rotated), now);
        audit.record(session.getUser(), "SESSION_REFRESHED", "AUTH_SESSION", session.getId(), "SUCCESS", metadata, null);
        return new TokenResponse("Bearer", tokens.accessToken(session.getUser(), session, now),
                tokens.accessTokenExpiresInSeconds(), rotated, session.getId(),
                !profiles.existsByUserId(session.getUser().getId()));
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> list(UUID userId, UUID currentSessionId) {
        return sessions.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(session -> new SessionResponse(session.getId(), session.getDeviceId(), session.getDeviceName(),
                        session.getIpAddress(), session.getCreatedAt(), session.getLastUsedAt(), session.getExpiresAt(),
                        session.getId().equals(currentSessionId), session.getRevokedAt() != null))
                .toList();
    }

    @Transactional
    public void revoke(UUID userId, UUID sessionId, RequestMetadata metadata) {
        AuthSession session = sessions.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found."));
        if (session.getRevokedAt() == null) {
            session.revoke(Instant.now());
            audit.record(session.getUser(), "SESSION_REVOKED", "AUTH_SESSION", session.getId(), "SUCCESS", metadata, null);
        }
    }

    private ApiException invalidRefresh() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "The session is invalid or expired.");
    }
}
