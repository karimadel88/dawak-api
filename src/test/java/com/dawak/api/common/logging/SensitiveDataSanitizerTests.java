package com.dawak.api.common.logging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataSanitizerTests {
    private final SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();

    @Test
    void redactsCredentialsOtpAndPatientInformationAtEveryDepth() {
        String input = """
                {
                  "phoneNumber":"01001234567",
                  "code":"473921",
                  "profile":{"firstName":"Karim","email":"patient@example.com"},
                  "tokens":[{"accessToken":"jwt","refresh_token":"refresh"}],
                  "error":{"code":"INVALID_OTP"},
                  "cityId":"10000000-0000-0000-0000-000000000001"
                }
                """;

        String sanitized = sanitizer.sanitizeJson(input.getBytes(StandardCharsets.UTF_8), 8192);

        assertThat(sanitized)
                .doesNotContain("01001234567", "473921", "Karim", "patient@example.com", "\":\"jwt\"", "\":\"refresh\"")
                .contains("\"phoneNumber\":\"[REDACTED]\"")
                .contains("\"code\":\"[REDACTED]\"")
                .contains("\"code\":\"INVALID_OTP\"")
                .contains("10000000-0000-0000-0000-000000000001");
    }

    @Test
    void omitsOversizedAndNonJsonPayloads() {
        assertThat(sanitizer.sanitizeJson("not-json".getBytes(StandardCharsets.UTF_8), 8192))
                .isEqualTo("[NON-JSON PAYLOAD OMITTED]");
        assertThat(sanitizer.sanitizeJson("{\"safe\":true}".getBytes(StandardCharsets.UTF_8), 5))
                .startsWith("[PAYLOAD OMITTED:");
    }
}
