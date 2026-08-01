package com.dawak.api.identity.api;

import com.dawak.api.common.security.AuthenticatedUser;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.api.dto.OtpRequest;
import com.dawak.api.identity.api.dto.OtpRequestResponse;
import com.dawak.api.identity.api.dto.OtpVerificationRequest;
import com.dawak.api.identity.api.dto.RefreshTokenRequest;
import com.dawak.api.identity.api.dto.SessionResponse;
import com.dawak.api.identity.api.dto.TokenResponse;
import com.dawak.api.identity.application.OtpAuthenticationService;
import com.dawak.api.identity.application.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final OtpAuthenticationService otp;
    private final SessionService sessions;

    public AuthController(OtpAuthenticationService otp, SessionService sessions) {
        this.otp = otp;
        this.sessions = sessions;
    }

    @PostMapping("/otp/requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    OtpRequestResponse requestOtp(@Valid @RequestBody OtpRequest body, HttpServletRequest request) {
        return otp.request(body.phoneNumber(), RequestMetadata.from(request));
    }

    @PostMapping("/otp/verifications")
    TokenResponse verifyOtp(@Valid @RequestBody OtpVerificationRequest body, HttpServletRequest request) {
        return otp.verify(body, RequestMetadata.from(request));
    }

    @PostMapping("/token/refresh")
    TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest body, HttpServletRequest request) {
        return sessions.refresh(body.refreshToken(), RequestMetadata.from(request));
    }

    @GetMapping("/sessions")
    List<SessionResponse> sessions(@AuthenticationPrincipal Jwt jwt) {
        return sessions.list(AuthenticatedUser.userId(jwt), AuthenticatedUser.sessionId(jwt));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@PathVariable UUID sessionId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        sessions.revoke(AuthenticatedUser.userId(jwt), sessionId, RequestMetadata.from(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        sessions.revoke(AuthenticatedUser.userId(jwt), AuthenticatedUser.sessionId(jwt), RequestMetadata.from(request));
    }
}
