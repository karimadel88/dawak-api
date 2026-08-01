package com.dawak.api.identity.application;

import com.dawak.api.identity.config.AuthProperties;
import com.dawak.api.identity.domain.AuthSession;
import com.dawak.api.identity.domain.User;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final AuthProperties properties;
    private final PatientProfileRepository profiles;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(JwtEncoder jwtEncoder, AuthProperties properties, PatientProfileRepository profiles) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.profiles = profiles;
    }

    public String accessToken(User user, AuthSession session, Instant now) {
        boolean onboardingComplete = profiles.existsByUserId(user.getId());
        var claims = JwtClaimsSet.builder()
                .issuer("dawak-api")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenTtl()))
                .claim("sid", session.getId().toString())
                .claim("roles", List.of("PATIENT"))
                .claim("onboardingComplete", onboardingComplete)
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String newRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String value) {
        try {
            return HexFormatHolder.hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public long accessTokenExpiresInSeconds() { return properties.accessTokenTtl().toSeconds(); }

    private static final class HexFormatHolder {
        static String hex(byte[] bytes) { return java.util.HexFormat.of().formatHex(bytes); }
    }
}
