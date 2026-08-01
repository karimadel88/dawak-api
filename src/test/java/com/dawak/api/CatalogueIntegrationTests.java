package com.dawak.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogueIntegrationTests {
    private static final UUID ADMIN_ID = UUID.fromString("90000000-0000-0000-0000-000000000001");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanCatalogue() {
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
        jdbc.update("delete from prescription");
        jdbc.update("delete from catalogue_idempotency_key");
        jdbc.update("delete from catalogue_status_history");
        jdbc.update("delete from medicine_alias");
        jdbc.update("delete from medicine_package");
        jdbc.update("delete from medicine_active_ingredient");
        jdbc.update("delete from medicine");
        jdbc.update("delete from active_ingredient");
        jdbc.update("delete from dosage_form");
        jdbc.update("delete from manufacturer");
        jdbc.update("delete from audit_event where aggregate_type='MEDICINE_PACKAGE'");
        jdbc.update("""
                insert into app_user(id,phone_number,phone_number_verified_at,status,preferred_language,
                  created_at,updated_at,version) values (?,'+201009999999',now(),'ACTIVE','ar',now(),now(),0)
                on conflict(id) do nothing
                """, ADMIN_ID);
    }

    @Test
    void catalogueManagerCreatesAndPublicSearchFindsExactArabicAndEnglishPackage() throws Exception {
        JsonNode created = createPackage(packageJson("200", "AVAILABLE", false), "create-package-001");
        String packageId = created.get("id").asString();

        assertThat(created.get("requestable").asBoolean()).isTrue();
        assertThat(created.get("prescriptionRequired").asBoolean()).isTrue();

        mvc.perform(get("/api/v1/medicines/search").param("q", "ايبوبروفين"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(packageId))
                .andExpect(jsonPath("$.items[0].strengthValue").value(200))
                .andExpect(jsonPath("$.items[0].dosageFormCode").value("TABLET"));

        mvc.perform(get("/api/v1/medicines/search").param("q", "ibupro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(packageId));

        mvc.perform(get("/api/v1/medicines/search").param("q", "بروفين"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].activeIngredients[0].code").value("IBUPROFEN"));

        mvc.perform(get("/api/v1/medicines/{id}", packageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameAr").value("إيبوبروفين"))
                .andExpect(jsonPath("$.packageSizeValue").value(20));

        assertThat(jdbc.queryForObject("select count(*) from catalogue_status_history", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from audit_event where event_type='MEDICINE_PACKAGE_CREATED'", Integer.class)).isEqualTo(1);
    }

    @Test
    void exactPackagesRemainDistinctAndDuplicatePackageIsRejected() throws Exception {
        JsonNode first = createPackage(packageJson("200", "AVAILABLE", false), null);
        JsonNode second = createPackage(packageJson("400", "AVAILABLE", false).replace("6221000000001", "6221000000002"), null);

        assertThat(first.get("id").asString()).isNotEqualTo(second.get("id").asString());
        mvc.perform(get("/api/v1/medicines/search").param("q", "Ibuprofen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items[0].medicineId").value(jsonPathValue(first, "medicineId")));

        mvc.perform(post("/api/v1/admin/catalogue/packages")
                        .with(managerJwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(packageJson("200", "AVAILABLE", false)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_MEDICINE_PACKAGE"));
    }

    @Test
    void restrictedAndDeactivatedPackagesCannotBeRequested() throws Exception {
        JsonNode restricted = createPackage(packageJson("200", "AVAILABLE", true), null);
        assertThat(restricted.get("requestable").asBoolean()).isFalse();
        assertThat(restricted.get("unavailableReason").asString()).isEqualTo("RESTRICTED");

        String id = restricted.get("id").asString();
        mvc.perform(delete("/api/v1/admin/catalogue/packages/{id}", id)
                        .with(managerJwt()).param("reason", "Supplier recall"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/medicines/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestable").value(false))
                .andExpect(jsonPath("$.unavailableReason").value("DISABLED"))
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"));
    }

    @Test
    void importsAreAtomicIdempotentAndProtectedByRole() throws Exception {
        String importBody = "{\"packages\":[" + packageJson("200", "AVAILABLE", false) + "]}";

        mvc.perform(post("/api/v1/admin/catalogue/imports")
                        .contentType(MediaType.APPLICATION_JSON).content(importBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/catalogue/imports")
                        .with(jwt().jwt(jwt -> jwt.subject(ADMIN_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                        .header("Idempotency-Key", "catalogue-import-001")
                        .contentType(MediaType.APPLICATION_JSON).content(importBody))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/admin/catalogue/imports")
                        .with(managerJwt()).header("Idempotency-Key", "catalogue-import-001")
                        .contentType(MediaType.APPLICATION_JSON).content(importBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.replayed").value(false));
        mvc.perform(post("/api/v1/admin/catalogue/imports")
                        .with(managerJwt()).header("Idempotency-Key", "catalogue-import-001")
                        .contentType(MediaType.APPLICATION_JSON).content(importBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.replayed").value(true));

        assertThat(jdbc.queryForObject("select count(*) from medicine_package", Integer.class)).isEqualTo(1);
    }

    @Test
    void searchValidatesAmbiguousQueriesAndPagination() throws Exception {
        mvc.perform(get("/api/v1/medicines/search").param("q", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SEARCH_QUERY_TOO_SHORT"));
        mvc.perform(get("/api/v1/medicines/search").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_SIZE"));
    }

    private JsonNode createPackage(String body, String idempotencyKey) throws Exception {
        var request = post("/api/v1/admin/catalogue/packages").with(managerJwt())
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (idempotencyKey != null) request.header("Idempotency-Key", idempotencyKey);
        String response = mvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor managerJwt() {
        return jwt().jwt(jwt -> jwt.subject(ADMIN_ID.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_CATALOGUE_MANAGER"));
    }

    private String packageJson(String strength, String status, boolean restricted) {
        return """
                {
                  "nameAr":"إيبوبروفين","nameEn":"Ibuprofen",
                  "manufacturerCode":"EVA","manufacturerNameAr":"إيفا فارما","manufacturerNameEn":"EVA Pharma",
                  "activeIngredients":[{"code":"IBUPROFEN","nameAr":"إيبوبروفين","nameEn":"Ibuprofen"}],
                  "strengthValue":%s,"strengthUnit":"MG",
                  "dosageFormCode":"TABLET","dosageFormNameAr":"أقراص","dosageFormNameEn":"Tablets",
                  "packageSizeValue":20,"packageSizeUnit":"TABLET","routeOfAdministration":"ORAL",
                  "barcode":"6221000000001","officialPrice":50.00,"currency":"EGP",
                  "prescriptionRequired":true,"restricted":%s,"restrictionCode":%s,
                  "storageType":"ROOM_TEMPERATURE","status":"%s","aliases":["بروفين"]
                }
                """.formatted(strength, restricted, restricted ? "\"CONTROLLED\"" : "null", status);
    }

    private String jsonPathValue(JsonNode node, String field) {
        return node.get(field).asString();
    }
}
