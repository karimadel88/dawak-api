package com.dawak.api;

import com.dawak.api.prescription.application.PrescriptionFileInspector;
import com.dawak.api.prescription.application.PrescriptionRetentionService;
import com.dawak.api.prescription.application.PrescriptionService;
import com.dawak.api.prescription.application.MalwareScanner;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.common.api.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrescriptionIntegrationTests {
    private static final UUID PATIENT = UUID.fromString("91000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_PATIENT = UUID.fromString("91000000-0000-0000-0000-000000000002");
    private static final UUID PHARMACIST = UUID.fromString("91000000-0000-0000-0000-000000000003");
    private static final UUID OTHER_PHARMACIST = UUID.fromString("91000000-0000-0000-0000-000000000004");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PrescriptionService prescriptions;
    @Autowired PrescriptionRetentionService retention;
    @MockitoBean MalwareScanner malwareScanner;

    @BeforeEach
    void setUp() {
        when(malwareScanner.scan(any())).thenAnswer(invocation -> {
            byte[] content = invocation.getArgument(0);
            String value = new String(content, StandardCharsets.ISO_8859_1);
            return value.contains("EICAR-STANDARD-ANTIVIRUS-TEST-FILE")
                    ? MalwareScanner.ScanResult.infectedResult("Eicar-Test-Signature")
                    : MalwareScanner.ScanResult.cleanResult();
        });
        jdbc.update("delete from request_pharmacy_match");
        jdbc.update("delete from medicine_request_status_history");
        jdbc.update("delete from medicine_request_idempotency_key");
        jdbc.update("delete from medicine_request_item");
        jdbc.update("delete from medicine_request");
        jdbc.update("delete from prescription_access_log");
        jdbc.update("delete from prescription_access_grant");
        jdbc.update("delete from prescription");
        jdbc.update("delete from audit_event where aggregate_type='PRESCRIPTION'");
        insertUser(PATIENT, "+201001111111");
        insertUser(OTHER_PATIENT, "+201001111112");
        insertUser(PHARMACIST, "+201001111113");
        insertUser(OTHER_PHARMACIST, "+201001111114");
        jdbc.update("""
                insert into patient_profile(id,user_id,first_name,last_name,city_id,area_id,created_at,updated_at,version)
                values ('92000000-0000-0000-0000-000000000001',?,'Test','Patient',
                '10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001',now(),now(),0)
                on conflict(user_id) do nothing
                """, PATIENT);
    }

    @Test
    void secureUploadClaimAccessAndReviewLifecycleIsAudited() throws Exception {
        byte[] pdf = "%PDF-1.7\nminimal prescription".getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(pdf, "application/pdf", "rx.pdf");
        String id = intent.get("prescriptionId").asString();
        String uploadToken = intent.get("uploadToken").asString();

        mvc.perform(put("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT))
                        .header("Upload-Token", uploadToken).contentType(MediaType.APPLICATION_OCTET_STREAM).content(pdf))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_REVIEW"));

        mvc.perform(get("/api/v1/prescriptions/{id}", id).with(patientJwt(OTHER_PATIENT)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pharmacist/prescriptions/{id}/claim", id).with(pharmacistJwt(PHARMACIST)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.designatedReviewerId").value(PHARMACIST.toString()));
        mvc.perform(post("/api/v1/pharmacist/prescriptions/{id}/claim", id).with(pharmacistJwt(OTHER_PHARMACIST)))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/v1/prescriptions/{id}/access-url", id).with(pharmacistJwt(OTHER_PHARMACIST))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"purpose\":\"PRESCRIPTION_REVIEW\"}"))
                .andExpect(status().isNotFound());
        String accessResponse = mvc.perform(post("/api/v1/prescriptions/{id}/access-url", id).with(pharmacistJwt(PHARMACIST))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"purpose\":\"PRESCRIPTION_REVIEW\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String accessToken = json.readTree(accessResponse).get("accessToken").asString();
        mvc.perform(get("/api/v1/prescriptions/{id}/content", id).with(pharmacistJwt(PHARMACIST))
                        .param("accessToken", accessToken))
                .andExpect(status().isOk()).andExpect(content().bytes(pdf))
                .andExpect(header().string("Cache-Control", "no-store"));

        mvc.perform(post("/api/v1/pharmacist/prescriptions/{id}/review", id).with(pharmacistJwt(PHARMACIST))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\",\"validUntil\":\"2099-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

        assertThat(jdbc.queryForObject("select count(*) from prescription_access_log where prescription_id=?", Integer.class,
                UUID.fromString(id))).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from audit_event where aggregate_id=?", Integer.class,
                UUID.fromString(id))).isGreaterThanOrEqualTo(7);
    }

    @Test
    void signatureFailureRemainsFailedAndCannotEnterReviewQueue() throws Exception {
        byte[] fakePdf = "this is not a pdf".getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(fakePdf, "application/pdf", "fake.pdf");
        String id = intent.get("prescriptionId").asString();
        mvc.perform(put("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT))
                        .header("Upload-Token", intent.get("uploadToken").asString())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(fakePdf))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PRESCRIPTION_FILE_SIGNATURE_INVALID"));

        assertThat(jdbc.queryForObject("select status from prescription where id=?", String.class, UUID.fromString(id)))
                .isEqualTo("SCAN_FAILED");
        mvc.perform(get("/api/v1/pharmacist/prescriptions/queue").with(pharmacistJwt(PHARMACIST)))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void malwarePositiveFileIsDeletedAndRecordedAsFailed() throws Exception {
        byte[] infected = "%PDF-1.7\nX5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE"
                .getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(infected, "application/pdf", "infected.pdf");
        String id = intent.get("prescriptionId").asString();
        mvc.perform(put("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT))
                        .header("Upload-Token", intent.get("uploadToken").asString())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(infected))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PRESCRIPTION_MALWARE_DETECTED"));
        assertThat(jdbc.queryForObject("select status from prescription where id=?", String.class, UUID.fromString(id)))
                .isEqualTo("SCAN_FAILED");
    }

    @Test
    void expiredAccessTokenIsDeniedAndRetentionRemovesObjectAndGrant() throws Exception {
        JsonNode ready = createReadyPrescription();
        String id = ready.get("id").asString();
        String accessJson = mvc.perform(post("/api/v1/prescriptions/{id}/access-url", id).with(patientJwt(PATIENT))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"purpose\":\"PATIENT_VIEW\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(accessJson).get("accessToken").asString();
        jdbc.update("update prescription_access_grant set expires_at=now()-interval '1 second' where prescription_id=?",
                UUID.fromString(id));
        mvc.perform(get("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT)).param("accessToken", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PRESCRIPTION_ACCESS_TOKEN_INVALID"));

        Instant cutoff = Instant.now();
        jdbc.update("update prescription set retention_until=? where id=?",
                java.sql.Timestamp.from(cutoff.minusSeconds(1)), UUID.fromString(id));
        var result = retention.cleanExpiredData(cutoff);
        assertThat(result.deletedPrescriptions()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select status from prescription where id=?", String.class, UUID.fromString(id)))
                .isEqualTo("DELETED");
        assertThat(jdbc.queryForObject("select count(*) from prescription_access_grant where prescription_id=?", Integer.class,
                UUID.fromString(id))).isZero();
    }

    @Test
    void concurrentPharmacistClaimsProduceExactlyOneDesignatedReviewer() throws Exception {
        UUID id = UUID.fromString(createReadyPrescription().get("id").asString());
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> claimAfter(start, id, PHARMACIST));
            var second = pool.submit(() -> claimAfter(start, id, OTHER_PHARMACIST));
            start.countDown();
            var outcomes = java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertThat(outcomes).containsExactlyInAnyOrder("SUCCESS", "PRESCRIPTION_CANNOT_BE_CLAIMED");
            assertThat(jdbc.queryForObject("select reviewed_by_user_id is not null from prescription where id=?",
                    Boolean.class, id)).isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void scannerOutageReturnsTraceableErrorPreservesQuarantineAndAllowsRetry() throws Exception {
        byte[] pdf = "%PDF-1.7\nretry after scanner outage".getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(pdf, "application/pdf", "rx.pdf");
        String id = intent.get("prescriptionId").asString();
        mvc.perform(put("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT))
                        .header("Upload-Token", intent.get("uploadToken").asString())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(pdf))
                .andExpect(status().isNoContent());

        doThrow(new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "PRESCRIPTION_SCANNER_UNAVAILABLE", "ClamAV is unavailable", new java.net.ConnectException("refused")))
                .when(malwareScanner).scan(any());
        mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT))
                        .header("X-Request-ID", "scanner-outage-test"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PRESCRIPTION_SCANNER_UNAVAILABLE"))
                .andExpect(jsonPath("$.requestId").value("scanner-outage-test"));
        assertThat(jdbc.queryForObject("select status from prescription where id=?", String.class, UUID.fromString(id)))
                .isEqualTo("QUARANTINED");

        doReturn(MalwareScanner.ScanResult.cleanResult()).when(malwareScanner).scan(any());
        mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_REVIEW"));
    }

    @Test
    void missingUploadHeaderReturnsSafeCodeAndRequestId() throws Exception {
        byte[] pdf = "%PDF-1.7\nmissing header".getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(pdf, "application/pdf", "rx.pdf");
        mvc.perform(put("/api/v1/prescriptions/{id}/content", intent.get("prescriptionId").asString())
                        .with(patientJwt(PATIENT)).header("X-Request-ID", "missing-header-test")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(pdf))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_HEADER"))
                .andExpect(jsonPath("$.requestId").value("missing-header-test"));
    }

    private JsonNode createIntent(byte[] content, String type, String filename) throws Exception {
        String body = """
                {"originalFilename":"%s","contentType":"%s","fileSize":%d,"checksumSha256":"%s"}
                """.formatted(filename, type, content.length, PrescriptionFileInspector.sha256(content));
        return json.readTree(mvc.perform(post("/api/v1/prescriptions/upload-intents").with(patientJwt(PATIENT))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode createReadyPrescription() throws Exception {
        byte[] pdf = "%PDF-1.7\nretention test".getBytes(StandardCharsets.US_ASCII);
        JsonNode intent = createIntent(pdf, "application/pdf", "rx.pdf");
        String id = intent.get("prescriptionId").asString();
        mvc.perform(put("/api/v1/prescriptions/{id}/content", id).with(patientJwt(PATIENT))
                        .header("Upload-Token", intent.get("uploadToken").asString())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(pdf))
                .andExpect(status().isNoContent());
        return json.readTree(mvc.perform(post("/api/v1/prescriptions/{id}/finalize", id).with(patientJwt(PATIENT)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private String claimAfter(CountDownLatch start, UUID prescriptionId, UUID pharmacistId) {
        try {
            start.await(5, TimeUnit.SECONDS);
            prescriptions.claim(prescriptionId, pharmacistId, new RequestMetadata("127.0.0.1", "test"));
            return "SUCCESS";
        } catch (ApiException exception) {
            return exception.getCode();
        } catch (Exception exception) {
            return exception.getClass().getSimpleName();
        }
    }

    private void insertUser(UUID id, String phone) {
        jdbc.update("""
                insert into app_user(id,phone_number,phone_number_verified_at,status,preferred_language,created_at,updated_at,version)
                values (?,?,now(),'ACTIVE','ar',now(),now(),0) on conflict(id) do nothing
                """, id, phone);
    }
    private org.springframework.test.web.servlet.request.RequestPostProcessor patientJwt(UUID id) {
        return jwt().jwt(value -> value.subject(id.toString())).authorities(new SimpleGrantedAuthority("ROLE_PATIENT"));
    }
    private org.springframework.test.web.servlet.request.RequestPostProcessor pharmacistJwt(UUID id) {
        return jwt().jwt(value -> value.subject(id.toString())).authorities(new SimpleGrantedAuthority("ROLE_PHARMACIST"));
    }
}
