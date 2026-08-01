package com.dawak.api;

import com.dawak.api.identity.persistence.UserRepository;
import com.dawak.api.patient.persistence.ConsentRecordRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientIdentityIntegrationTests {
    private static final String CAIRO_ID = "10000000-0000-0000-0000-000000000001";
    private static final String NASR_CITY_ID = "20000000-0000-0000-0000-000000000001";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CapturingOtpDelivery otpDelivery;
    @Autowired UserRepository users;
    @Autowired PatientProfileRepository profiles;
    @Autowired ConsentRecordRepository consents;
    @Autowired JdbcTemplate jdbc;

    @Test
    void patientCanRegisterManageProfileConsentAndSessions() throws Exception {
        String phone = "+201001234501";
        JsonNode challenge = requestOtp(phone);
        JsonNode tokens = verify(phone, challenge.get("challengeId").asString(), otpDelivery.codeFor(phone), "pixel-1");
        String accessToken = tokens.get("accessToken").asString();
        String refreshToken = tokens.get("refreshToken").asString();

        assertThat(tokens.get("onboardingRequired").asBoolean()).isTrue();

        mvc.perform(post("/api/v1/patient/profile/complete")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Karim", "Adel", "1.0", "1.0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredLanguage").value("ar"))
                .andExpect(jsonPath("$.cityNameAr").value("القاهرة"));

        mvc.perform(get("/api/v1/patient/profile").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(phone))
                .andExpect(jsonPath("$.firstName").value("Karim"));

        mvc.perform(patch("/api/v1/patient/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateProfileJson("كريم", "عادل")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("كريم"));

        mvc.perform(get("/api/v1/patient/profile/consents").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/v1/auth/sessions").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].current").value(true));

        String refreshResponse = mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rotatedRefreshToken = json.readTree(refreshResponse).get("refreshToken").asString();

        mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rotatedRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        mvc.perform(post("/api/v1/auth/otp/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationJson(phone, challenge.get("challengeId").asString(), otpDelivery.codeFor(phone), "pixel-2")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("OTP_REPLAYED"));
    }

    @Test
    void invalidAndOverAttemptOtpDoesNotCreateAccount() throws Exception {
        String phone = "+201001234502";
        String challengeId = requestOtp(phone).get("challengeId").asString();

        for (int attempt = 1; attempt <= 4; attempt++) {
            mvc.perform(post("/api/v1/auth/otp/verifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(verificationJson(phone, challengeId, "999999", "device-x")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_OTP"));
        }
        mvc.perform(post("/api/v1/auth/otp/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationJson(phone, challengeId, "999999", "device-x")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("OTP_ATTEMPTS_EXCEEDED"));

        assertThat(users.findByPhoneNumber(phone)).isEmpty();
    }

    @Test
    void registrationRequiresCurrentTermsAndPrivacyConsent() throws Exception {
        String phone = "+201001234503";
        JsonNode challenge = requestOtp(phone);
        JsonNode tokens = verify(phone, challenge.get("challengeId").asString(), otpDelivery.codeFor(phone), "device-policy");
        String accessToken = tokens.get("accessToken").asString();

        mvc.perform(post("/api/v1/patient/profile/complete")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Mona", "Ali", "0.9", "1.0")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POLICY_VERSION_OUTDATED"));

        var user = users.findByPhoneNumber(phone).orElseThrow();
        assertThat(profiles.existsByUserId(user.getId())).isFalse();
        assertThat(consents.findAllByUserIdOrderByGrantedAtDesc(user.getId())).isEmpty();
    }

    @Test
    void expiredOtpIsRejectedWithoutCreatingAccount() throws Exception {
        String phone = "+201001234505";
        String challengeId = requestOtp(phone).get("challengeId").asString();
        jdbc.update("update otp_challenge set expires_at = now() - interval '1 second' where id = ?::uuid", challengeId);

        mvc.perform(post("/api/v1/auth/otp/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationJson(phone, challengeId, otpDelivery.codeFor(phone), "expired-device")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("OTP_EXPIRED"));

        assertThat(users.findByPhoneNumber(phone)).isEmpty();
    }

    @Test
    void otpRequestsAreRateLimitedWithoutRevealingAccountExistence() throws Exception {
        String phone = "+201001234504";
        for (int request = 0; request < 5; request++) requestOtp(phone);
        mvc.perform(post("/api/v1/auth/otp/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phone + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("OTP_RATE_LIMITED"));
    }

    private JsonNode requestOtp(String phone) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/otp/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phone + "\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.expiresInSeconds").value(300))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private JsonNode verify(String phone, String challengeId, String code, String deviceId) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/otp/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationJson(phone, challengeId, code, deviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private String verificationJson(String phone, String challengeId, String code, String deviceId) {
        return """
                {"challengeId":"%s","phoneNumber":"%s","code":"%s","deviceId":"%s","deviceName":"Android"}
                """.formatted(challengeId, phone, code, deviceId);
    }

    private String profileJson(String firstName, String lastName, String terms, String privacy) {
        return """
                {"firstName":"%s","lastName":"%s","email":"patient@example.com","birthYear":1990,
                 "preferredLanguage":"ar","cityId":"%s","areaId":"%s",
                 "acceptedTermsVersion":"%s","acceptedPrivacyVersion":"%s"}
                """.formatted(firstName, lastName, CAIRO_ID, NASR_CITY_ID, terms, privacy);
    }

    private String updateProfileJson(String firstName, String lastName) {
        return """
                {"firstName":"%s","lastName":"%s","email":"patient@example.com","birthYear":1990,
                 "preferredLanguage":"ar","cityId":"%s","areaId":"%s"}
                """.formatted(firstName, lastName, CAIRO_ID, NASR_CITY_ID);
    }
}
