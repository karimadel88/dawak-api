package com.dawak.api;

import com.dawak.api.request.application.MedicineRequestService;
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
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicineRequestIntegrationTests {
    private static final UUID PATIENT = UUID.fromString("93000000-0000-0000-0000-000000000001");
    private static final UUID PROFILE = UUID.fromString("93000000-0000-0000-0000-000000000002");
    private static final UUID OTC_PACKAGE = UUID.fromString("93000000-0000-0000-0000-000000000010");
    private static final UUID RX_PACKAGE = UUID.fromString("93000000-0000-0000-0000-000000000011");
    private static final UUID OTHER_RX_PACKAGE = UUID.fromString("93000000-0000-0000-0000-000000000012");
    private static final UUID CITY = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID AREA = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MedicineRequestService requests;

    @BeforeEach
    void setUp() {
        jdbc.update("delete from request_pharmacy_match");
        jdbc.update("delete from medicine_request_status_history");
        jdbc.update("delete from medicine_request_idempotency_key");
        jdbc.update("delete from medicine_request_item");
        jdbc.update("delete from medicine_request");
        jdbc.update("delete from pharmacy_branch_service_area");
        jdbc.update("delete from pharmacy_branch");
        jdbc.update("delete from pharmacy");
        jdbc.update("delete from prescription_access_log");
        jdbc.update("delete from prescription_access_grant");
        jdbc.update("delete from prescription where patient_profile_id=?", PROFILE);
        jdbc.update("delete from audit_event where aggregate_type='MEDICINE_REQUEST'");
        seedPatientAndCatalogue();
    }

    @Test
    void submissionMatchesOnlyEligibleBranchesAndRetriesAreIdempotent() throws Exception {
        seedBranch("93000000-0000-0000-0000-000000000021", "APPROVED", "ACTIVE", true, true, 3);
        seedBranch("93000000-0000-0000-0000-000000000022", "SUSPENDED", "ACTIVE", true, true, 2);
        seedBranch("93000000-0000-0000-0000-000000000023", "APPROVED", "TEMPORARILY_INACTIVE", true, true, 1);

        JsonNode created = create(OTC_PACKAGE, null, "create-otc-1");
        JsonNode replay = create(OTC_PACKAGE, null, "create-otc-1");
        assertThat(replay.get("id").asString()).isEqualTo(created.get("id").asString());
        assertThat(replay.get("replayed").asBoolean()).isTrue();

        String id = created.get("id").asString();
        mvc.perform(post("/api/v1/medicine-requests/{id}/submit", id).with(patientJwt())
                        .header("Idempotency-Key", "submit-otc-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("MATCHING"))
                .andExpect(jsonPath("$.matchedBranchCount").value(1));
        mvc.perform(post("/api/v1/medicine-requests/{id}/submit", id).with(patientJwt())
                        .header("Idempotency-Key", "submit-otc-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.replayed").value(true));

        assertThat(jdbc.queryForObject("select count(*) from medicine_request", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from request_pharmacy_match", Integer.class)).isEqualTo(1);
    }

    @Test
    void prescriptionMedicineAndApprovalAreRequiredBeforeMatching() throws Exception {
        seedBranch("93000000-0000-0000-0000-000000000024", "APPROVED", "ACTIVE", true, true, 2);
        String requestId = create(RX_PACKAGE, null, "create-rx-1").get("id").asString();
        mvc.perform(post("/api/v1/medicine-requests/{id}/submit", requestId).with(patientJwt())
                        .header("Idempotency-Key", "submit-rx-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AWAITING_PRESCRIPTION"));

        UUID wrong = seedPrescription(OTHER_RX_PACKAGE, "APPROVED");
        mvc.perform(post("/api/v1/medicine-requests/{id}/qualify", requestId).with(patientJwt())
                        .header("Idempotency-Key", "qualify-rx-wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prescriptionId\":\"" + wrong + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PRESCRIPTION_MEDICINE_MISMATCH"));

        UUID correct = seedPrescription(RX_PACKAGE, "PENDING_REVIEW");
        mvc.perform(post("/api/v1/medicine-requests/{id}/qualify", requestId).with(patientJwt())
                        .header("Idempotency-Key", "qualify-rx-pending")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prescriptionId\":\"" + correct + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_PRESCRIPTION_REVIEW"));

        jdbc.update("update prescription set status='APPROVED',valid_until=now()+interval '1 day' where id=?", correct);
        mvc.perform(post("/api/v1/medicine-requests/{id}/qualify", requestId).with(patientJwt())
                        .header("Idempotency-Key", "qualify-rx-approved")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prescriptionId\":\"" + correct + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("MATCHING"))
                .andExpect(jsonPath("$.matchedBranchCount").value(1));
    }

    @Test
    void noEligibleBranchProducesUnfulfilledAndLargerRadiusCanRematch() throws Exception {
        seedBranch("93000000-0000-0000-0000-000000000025", "APPROVED", "ACTIVE", true, false, 15);
        String id = create(OTC_PACKAGE, null, "create-radius-1").get("id").asString();
        mvc.perform(post("/api/v1/medicine-requests/{id}/submit", id).with(patientJwt())
                        .header("Idempotency-Key", "submit-radius-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNFULFILLED"))
                .andExpect(jsonPath("$.matchedBranchCount").value(0));
        mvc.perform(post("/api/v1/medicine-requests/{id}/expand-radius", id).with(patientJwt())
                        .header("Idempotency-Key", "expand-radius-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"radiusKm\":20}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("MATCHING"))
                .andExpect(jsonPath("$.matchedBranchCount").value(1));
    }

    @Test
    void dueActiveRequestIsExpiredByLifecycleCleanup() throws Exception {
        String id = create(OTC_PACKAGE, null, "create-expiry-1").get("id").asString();
        mvc.perform(post("/api/v1/medicine-requests/{id}/submit", id).with(patientJwt())
                        .header("Idempotency-Key", "submit-expiry-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNFULFILLED"));
        jdbc.update("update medicine_request set expires_at=now()-interval '1 second' where id=?", UUID.fromString(id));

        requests.expireDueRequests();

        assertThat(jdbc.queryForObject("select status from medicine_request where id=?", String.class, UUID.fromString(id)))
                .isEqualTo("EXPIRED");
    }

    private JsonNode create(UUID packageId, UUID prescriptionId, String key) throws Exception {
        String body = """
                {"medicinePackageId":"%s","quantity":2,"cityId":"%s","areaId":"%s",
                 "fulfillmentPreference":"PICKUP","urgency":"NORMAL","prescriptionId":%s}
                """.formatted(packageId, CITY, AREA, prescriptionId == null ? "null" : "\"" + prescriptionId + "\"");
        String response = mvc.perform(post("/api/v1/medicine-requests").with(patientJwt())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private void seedPatientAndCatalogue() {
        jdbc.update("""
                insert into app_user(id,phone_number,phone_number_verified_at,status,preferred_language,created_at,updated_at,version)
                values (?,'+201003333331',now(),'ACTIVE','ar',now(),now(),0) on conflict(id) do nothing
                """, PATIENT);
        jdbc.update("""
                insert into patient_profile(id,user_id,first_name,last_name,city_id,area_id,created_at,updated_at,version)
                values (?,?,'Request','Patient',?,?,now(),now(),0) on conflict(id) do nothing
                """, PROFILE, PATIENT, CITY, AREA);
        jdbc.update("""
                insert into manufacturer(id,code,name_ar,name_en,normalized_name,active,created_at,updated_at,version)
                values ('93000000-0000-0000-0000-000000000030','REQUEST_TEST','اختبار','Request Test','request test',true,now(),now(),0)
                on conflict(id) do nothing
                """);
        jdbc.update("""
                insert into dosage_form(id,code,name_ar,name_en,active,created_at,updated_at,version)
                values ('93000000-0000-0000-0000-000000000031','REQUEST_TABLET','قرص','Tablet',true,now(),now(),0)
                on conflict(id) do nothing
                """);
        seedMedicineAndPackage("93000000-0000-0000-0000-000000000040", OTC_PACKAGE, false, "930000000010");
        seedMedicineAndPackage("93000000-0000-0000-0000-000000000041", RX_PACKAGE, true, "930000000011");
        seedMedicineAndPackage("93000000-0000-0000-0000-000000000042", OTHER_RX_PACKAGE, true, "930000000012");
    }

    private void seedMedicineAndPackage(String medicineId, UUID packageId, boolean prescriptionRequired, String barcode) {
        jdbc.update("""
                insert into medicine(id,name_ar,name_en,normalized_name_ar,normalized_name_en,manufacturer_id,
                  prescription_required,restricted,active,created_at,updated_at,version)
                values (?,'دواء اختبار','Request medicine','دواء اختبار','request medicine',
                  '93000000-0000-0000-0000-000000000030',?,false,true,now(),now(),0) on conflict(id) do nothing
                """, UUID.fromString(medicineId), prescriptionRequired);
        jdbc.update("""
                insert into medicine_package(id,medicine_id,strength_value,strength_unit,dosage_form_id,
                  package_size_value,package_size_unit,barcode,status,active,search_text,created_at,updated_at,version)
                values (?,?,100,'MG','93000000-0000-0000-0000-000000000031',10,'TABLET',?,'AVAILABLE',true,
                  'request medicine',now(),now(),0) on conflict(id) do nothing
                """, packageId, UUID.fromString(medicineId), barcode);
    }

    private void seedBranch(String suffix, String pharmacyStatus, String branchStatus,
                            boolean accepting, boolean prescriptionHandling, int distance) {
        UUID branchId = UUID.fromString(suffix);
        UUID pharmacyId = UUID.nameUUIDFromBytes(("pharmacy-" + suffix).getBytes());
        jdbc.update("""
                insert into pharmacy(id,public_name,license_number,license_expiry_date,status,created_at,updated_at,version)
                values (?, 'Test pharmacy', ?, current_date+30, ?, now(),now(),0)
                """, pharmacyId, "LIC-" + suffix.substring(suffix.length() - 4), pharmacyStatus);
        jdbc.update("""
                insert into pharmacy_branch(id,pharmacy_id,branch_code,name,status,city_id,area_id,pickup_enabled,
                  delivery_enabled,prescription_handling_enabled,accepting_requests,created_at,updated_at,version)
                values (?,?,?,'Test branch',?,?,?,true,false,?,?,now(),now(),0)
                """, branchId, pharmacyId, "B-" + suffix.substring(suffix.length() - 4), branchStatus,
                CITY, AREA, prescriptionHandling, accepting);
        jdbc.update("insert into pharmacy_branch_service_area(pharmacy_branch_id,area_id,distance_km) values (?,?,?)",
                branchId, AREA, distance);
    }

    private UUID seedPrescription(UUID packageId, String status) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into prescription(id,patient_profile_id,storage_key,original_filename,declared_content_type,
                  detected_content_type,file_size,checksum_sha256,status,retention_until,medicine_package_id,
                  created_at,updated_at,version)
                values (?,? ,?,'rx.pdf','application/pdf','application/pdf',100,?, ?,now()+interval '365 days',?,now(),now(),0)
                """, id, PROFILE, "test/" + id, "a".repeat(64), status, packageId);
        return id;
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor patientJwt() {
        return jwt().jwt(value -> value.subject(PATIENT.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_PATIENT"));
    }
}
