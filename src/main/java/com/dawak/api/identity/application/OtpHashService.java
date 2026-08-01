package com.dawak.api.identity.application;

import com.dawak.api.identity.config.AuthProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class OtpHashService {
    private final byte[] pepper;

    public OtpHashService(AuthProperties properties) {
        this.pepper = properties.otpPepper().getBytes(StandardCharsets.UTF_8);
    }

    public String hash(String phoneNumber, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal((phoneNumber + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash OTP", e);
        }
    }

    public boolean matches(String expectedHex, String actualHex) {
        return MessageDigest.isEqual(HexFormat.of().parseHex(expectedHex), HexFormat.of().parseHex(actualHex));
    }
}
