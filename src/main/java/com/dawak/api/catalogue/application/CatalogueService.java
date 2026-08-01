package com.dawak.api.catalogue.application;

import com.dawak.api.catalogue.api.dto.CatalogueImportRequest;
import com.dawak.api.catalogue.api.dto.CatalogueImportResponse;
import com.dawak.api.catalogue.api.dto.IngredientInput;
import com.dawak.api.catalogue.api.dto.MedicinePackageResponse;
import com.dawak.api.catalogue.api.dto.MedicinePackageWriteRequest;
import com.dawak.api.catalogue.api.dto.PageResponse;
import com.dawak.api.catalogue.domain.MedicinePackageStatus;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CatalogueService {
    private static final int MAX_PAGE_SIZE = 50;

    private final JdbcClient jdbc;
    private final MedicineSearchNormalizer normalizer;

    public CatalogueService(JdbcClient jdbc, MedicineSearchNormalizer normalizer) {
        this.jdbc = jdbc;
        this.normalizer = normalizer;
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicinePackageResponse> search(String rawQuery, int page, int size,
                                                         String dosageForm, UUID manufacturerId,
                                                         Boolean prescriptionRequired) {
        if (page < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Page must be zero or greater.");
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "Page size must be between 1 and 50.");
        }
        String query = normalizer.normalize(rawQuery);
        String barcode = clean(rawQuery);
        if (!query.isEmpty() && query.length() < 2 && !barcode.matches("\\d{6,80}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SEARCH_QUERY_TOO_SHORT", "Enter at least two search characters.");
        }

        String filters = """
                 where (:query = '' or mp.search_text like ('%' || :query || '%')
                    or mp.search_text % :query or (:barcode <> '' and mp.barcode = :barcode))
                   and (:dosage_form = '' or df.code = :dosage_form)
                   and (cast(:manufacturer_id as uuid) is null or mf.id = cast(:manufacturer_id as uuid))
                   and (cast(:prescription_required as boolean) is null
                        or m.prescription_required = cast(:prescription_required as boolean))
                """;
        var base = jdbc.sql("select mp.id from medicine_package mp join medicine m on m.id=mp.medicine_id " +
                        "join manufacturer mf on mf.id=m.manufacturer_id join dosage_form df on df.id=mp.dosage_form_id " +
                        filters + " order by case when :query='' then 0 else similarity(mp.search_text,:query) end desc, " +
                        "m.name_ar, mp.strength_value, mp.package_size_value limit :limit offset :offset")
                .param("query", query).param("barcode", barcode)
                .param("dosage_form", upper(dosageForm))
                .param("manufacturer_id", manufacturerId == null ? null : manufacturerId.toString())
                .param("prescription_required", prescriptionRequired)
                .param("limit", size).param("offset", page * size);
        List<UUID> ids = base.query(UUID.class).list();

        long total = jdbc.sql("select count(*) from medicine_package mp join medicine m on m.id=mp.medicine_id " +
                        "join manufacturer mf on mf.id=m.manufacturer_id join dosage_form df on df.id=mp.dosage_form_id " + filters)
                .param("query", query).param("barcode", barcode)
                .param("dosage_form", upper(dosageForm))
                .param("manufacturer_id", manufacturerId == null ? null : manufacturerId.toString())
                .param("prescription_required", prescriptionRequired)
                .query(Long.class).single();
        return PageResponse.of(ids.stream().map(this::find).toList(), page, size, total);
    }

    @Transactional(readOnly = true)
    public MedicinePackageResponse find(UUID packageId) {
        var rows = jdbc.sql("""
                select mp.id, mp.medicine_id, m.name_ar, m.name_en, mf.id manufacturer_id,
                       mf.name_ar manufacturer_name_ar, mf.name_en manufacturer_name_en,
                       mp.strength_value, mp.strength_unit, df.code dosage_form_code,
                       df.name_ar dosage_form_name_ar, df.name_en dosage_form_name_en,
                       mp.package_size_value, mp.package_size_unit, mp.route_of_administration,
                       mp.barcode, mp.official_price, mp.currency, m.prescription_required,
                       m.restricted, m.storage_type, mp.status, mp.active,
                       m.active medicine_active, mp.updated_at
                  from medicine_package mp
                  join medicine m on m.id=mp.medicine_id
                  join manufacturer mf on mf.id=m.manufacturer_id
                  join dosage_form df on df.id=mp.dosage_form_id
                 where mp.id=:id
                """).param("id", packageId).query((rs, row) -> {
            MedicinePackageStatus status = MedicinePackageStatus.valueOf(rs.getString("status"));
            boolean packageActive = rs.getBoolean("active");
            boolean medicineActive = rs.getBoolean("medicine_active");
            boolean restricted = rs.getBoolean("restricted");
            boolean requestable = packageActive && medicineActive && !restricted && status == MedicinePackageStatus.AVAILABLE;
            String unavailableReason = requestable ? null : unavailableReason(packageActive, medicineActive, restricted, status);
            return new MedicinePackageResponse(
                    rs.getObject("id", UUID.class), rs.getObject("medicine_id", UUID.class),
                    rs.getString("name_ar"), rs.getString("name_en"),
                    rs.getObject("manufacturer_id", UUID.class), rs.getString("manufacturer_name_ar"),
                    rs.getString("manufacturer_name_en"), ingredients(packageId),
                    rs.getBigDecimal("strength_value"), rs.getString("strength_unit"),
                    rs.getString("dosage_form_code"), rs.getString("dosage_form_name_ar"),
                    rs.getString("dosage_form_name_en"), rs.getBigDecimal("package_size_value"),
                    rs.getString("package_size_unit"), rs.getString("route_of_administration"),
                    rs.getString("barcode"), rs.getBigDecimal("official_price"), rs.getString("currency"),
                    rs.getBoolean("prescription_required"), restricted, rs.getString("storage_type"),
                    status, packageActive && medicineActive, requestable, unavailableReason,
                    rs.getTimestamp("updated_at").toInstant());
        }).list();
        if (rows.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_PACKAGE_NOT_FOUND", "Medicine package not found.");
        return rows.getFirst();
    }

    @Transactional
    public MedicinePackageResponse create(MedicinePackageWriteRequest request, String idempotencyKey,
                                          UUID actorId, RequestMetadata metadata) {
        validate(request);
        String hash = hash(request.toString());
        List<UUID> replay = replay(idempotencyKey, "CREATE", hash);
        if (replay != null) return find(replay.getFirst());
        UUID id = createInternal(request, actorId, metadata);
        remember(idempotencyKey, "CREATE", hash, List.of(id));
        return find(id);
    }

    @Transactional
    public CatalogueImportResponse importPackages(CatalogueImportRequest request, String idempotencyKey,
                                                   UUID actorId, RequestMetadata metadata) {
        requireIdempotencyKey(idempotencyKey);
        String hash = hash(request.toString());
        List<UUID> replay = replay(idempotencyKey, "IMPORT", hash);
        if (replay != null) return new CatalogueImportResponse(replay.size(), replay, true);

        Set<String> barcodes = new HashSet<>();
        request.packages().forEach(item -> {
            validate(item);
            String barcode = clean(item.barcode());
            if (!barcode.isEmpty() && !barcodes.add(barcode)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_IMPORT_BARCODE",
                        "The import contains a duplicate barcode: " + barcode);
            }
        });
        List<UUID> ids = new ArrayList<>();
        request.packages().forEach(item -> ids.add(createInternal(item, actorId, metadata)));
        remember(idempotencyKey, "IMPORT", hash, ids);
        return new CatalogueImportResponse(ids.size(), ids, false);
    }

    @Transactional
    public MedicinePackageResponse update(UUID packageId, MedicinePackageWriteRequest request,
                                          UUID actorId, RequestMetadata metadata) {
        validate(request);
        UUID medicineId = jdbc.sql("select medicine_id from medicine_package where id=:id")
                .param("id", packageId).query(UUID.class).optional()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_PACKAGE_NOT_FOUND", "Medicine package not found."));
        UUID manufacturerId = upsertManufacturer(request);
        UUID dosageFormId = upsertDosageForm(request);
        List<UUID> ingredientIds = request.activeIngredients().stream().map(this::upsertIngredient).toList();
        String oldStatus = jdbc.sql("select status from medicine_package where id=:id").param("id", packageId)
                .query(String.class).single();
        boolean oldActive = jdbc.sql("select active from medicine_package where id=:id").param("id", packageId)
                .query(Boolean.class).single();

        jdbc.sql("""
                update medicine set name_ar=:name_ar, name_en=:name_en, normalized_name_ar=:normalized_ar,
                       normalized_name_en=:normalized_en, manufacturer_id=:manufacturer_id,
                       prescription_required=:prescription_required, restricted=:restricted,
                       restriction_code=:restriction_code, storage_type=:storage_type, updated_at=now(), version=version+1
                 where id=:id
                """).param("name_ar", clean(request.nameAr())).param("name_en", clean(request.nameEn()))
                .param("normalized_ar", normalizer.normalize(request.nameAr()))
                .param("normalized_en", normalizer.normalize(request.nameEn()))
                .param("manufacturer_id", manufacturerId).param("prescription_required", request.prescriptionRequired())
                .param("restricted", request.restricted()).param("restriction_code", nullable(request.restrictionCode()))
                .param("storage_type", nullable(request.storageType())).param("id", medicineId).update();
        replaceIngredients(medicineId, ingredientIds);
        replaceAliases(medicineId, request.aliases());
        try {
            jdbc.sql("""
                    update medicine_package set strength_value=:strength, strength_unit=:strength_unit,
                           dosage_form_id=:dosage_form_id, package_size_value=:package_size,
                           package_size_unit=:package_unit, route_of_administration=:route, barcode=:barcode,
                           official_price=:price, currency=:currency, status=:status, active=true,
                           updated_at=now(), version=version+1 where id=:id
                    """).param("strength", request.strengthValue()).param("strength_unit", upper(request.strengthUnit()))
                    .param("dosage_form_id", dosageFormId).param("package_size", request.packageSizeValue())
                    .param("package_unit", upper(request.packageSizeUnit())).param("route", nullable(request.routeOfAdministration()))
                    .param("barcode", nullable(request.barcode())).param("price", request.officialPrice())
                    .param("currency", request.currency() == null ? null : upper(request.currency()))
                    .param("status", request.status().name()).param("id", packageId).update();
        } catch (DataIntegrityViolationException exception) {
            throw duplicatePackage();
        }
        refreshSearchText(medicineId);
        history(packageId, oldStatus, request.status().name(), oldActive, true, actorId, "Catalogue package updated");
        audit(actorId, "MEDICINE_PACKAGE_UPDATED", packageId, metadata, "status=" + request.status());
        return find(packageId);
    }

    @Transactional
    public void deactivate(UUID packageId, String reason, UUID actorId, RequestMetadata metadata) {
        var current = jdbc.sql("select status, active from medicine_package where id=:id").param("id", packageId)
                .query((rs, row) -> new Object[]{rs.getString("status"), rs.getBoolean("active")}).optional()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEDICINE_PACKAGE_NOT_FOUND", "Medicine package not found."));
        jdbc.sql("update medicine_package set active=false, status='UNAVAILABLE', updated_at=now(), version=version+1 where id=:id")
                .param("id", packageId).update();
        history(packageId, (String) current[0], MedicinePackageStatus.UNAVAILABLE.name(), (Boolean) current[1], false,
                actorId, nullable(reason));
        audit(actorId, "MEDICINE_PACKAGE_DEACTIVATED", packageId, metadata, "reason=" + clean(reason));
    }

    private UUID createInternal(MedicinePackageWriteRequest request, UUID actorId, RequestMetadata metadata) {
        UUID manufacturerId = upsertManufacturer(request);
        UUID dosageFormId = upsertDosageForm(request);
        List<UUID> ingredientIds = request.activeIngredients().stream().map(this::upsertIngredient).toList();
        UUID medicineId = findOrCreateMedicine(request, manufacturerId, ingredientIds);
        replaceAliases(medicineId, request.aliases());
        UUID packageId = UUID.randomUUID();
        try {
            jdbc.sql("""
                    insert into medicine_package(id, medicine_id, strength_value, strength_unit, dosage_form_id,
                      package_size_value, package_size_unit, route_of_administration, barcode, official_price,
                      currency, status, active, search_text, created_at, updated_at, version)
                    values (:id,:medicine_id,:strength,:strength_unit,:dosage_form_id,:package_size,:package_unit,
                      :route,:barcode,:price,:currency,:status,true,'',now(),now(),0)
                    """).param("id", packageId).param("medicine_id", medicineId)
                    .param("strength", request.strengthValue()).param("strength_unit", upper(request.strengthUnit()))
                    .param("dosage_form_id", dosageFormId).param("package_size", request.packageSizeValue())
                    .param("package_unit", upper(request.packageSizeUnit())).param("route", nullable(request.routeOfAdministration()))
                    .param("barcode", nullable(request.barcode())).param("price", request.officialPrice())
                    .param("currency", request.currency() == null ? null : upper(request.currency()))
                    .param("status", request.status().name()).update();
        } catch (DataIntegrityViolationException exception) {
            throw duplicatePackage();
        }
        refreshSearchText(medicineId);
        history(packageId, null, request.status().name(), null, true, actorId, "Catalogue package created");
        audit(actorId, "MEDICINE_PACKAGE_CREATED", packageId, metadata, "status=" + request.status());
        return packageId;
    }

    private UUID findOrCreateMedicine(MedicinePackageWriteRequest request, UUID manufacturerId, List<UUID> ingredientIds) {
        String normalizedAr = normalizer.normalize(request.nameAr());
        String normalizedEn = normalizer.normalize(request.nameEn());
        var existing = jdbc.sql("""
                select id, prescription_required, restricted, coalesce(restriction_code,''), coalesce(storage_type,'')
                  from medicine where normalized_name_ar=:ar and normalized_name_en=:en and manufacturer_id=:manufacturer
                """).param("ar", normalizedAr).param("en", normalizedEn).param("manufacturer", manufacturerId)
                .query((rs, row) -> new Object[]{rs.getObject("id", UUID.class), rs.getBoolean(2), rs.getBoolean(3),
                        rs.getString(4), rs.getString(5)}).optional();
        if (existing.isPresent()) {
            Object[] row = existing.get();
            boolean same = (Boolean) row[1] == request.prescriptionRequired() && (Boolean) row[2] == request.restricted()
                    && row[3].equals(clean(request.restrictionCode())) && row[4].equals(clean(request.storageType()));
            if (!same || !new HashSet<>(ingredientIds((UUID) row[0])).equals(new HashSet<>(ingredientIds))) {
                throw new ApiException(HttpStatus.CONFLICT, "MEDICINE_DEFINITION_CONFLICT",
                        "A medicine with these names already exists with different clinical attributes.");
            }
            return (UUID) row[0];
        }
        UUID medicineId = UUID.randomUUID();
        jdbc.sql("""
                insert into medicine(id,name_ar,name_en,normalized_name_ar,normalized_name_en,manufacturer_id,
                  prescription_required,restricted,restriction_code,storage_type,active,created_at,updated_at,version)
                values (:id,:name_ar,:name_en,:normalized_ar,:normalized_en,:manufacturer_id,
                  :prescription_required,:restricted,:restriction_code,:storage_type,true,now(),now(),0)
                """).param("id", medicineId).param("name_ar", clean(request.nameAr())).param("name_en", clean(request.nameEn()))
                .param("normalized_ar", normalizedAr).param("normalized_en", normalizedEn)
                .param("manufacturer_id", manufacturerId).param("prescription_required", request.prescriptionRequired())
                .param("restricted", request.restricted()).param("restriction_code", nullable(request.restrictionCode()))
                .param("storage_type", nullable(request.storageType())).update();
        replaceIngredients(medicineId, ingredientIds);
        return medicineId;
    }

    private UUID upsertManufacturer(MedicinePackageWriteRequest request) {
        return jdbc.sql("""
                insert into manufacturer(id,code,name_ar,name_en,normalized_name,active,created_at,updated_at,version)
                values (:id,:code,:name_ar,:name_en,:normalized,true,now(),now(),0)
                on conflict(code) do update set name_ar=excluded.name_ar,name_en=excluded.name_en,
                  normalized_name=excluded.normalized_name,active=true,updated_at=now(),version=manufacturer.version+1
                returning id
                """).param("id", UUID.randomUUID()).param("code", upper(request.manufacturerCode()))
                .param("name_ar", clean(request.manufacturerNameAr())).param("name_en", clean(request.manufacturerNameEn()))
                .param("normalized", normalizer.normalize(request.manufacturerNameAr()) + " " + normalizer.normalize(request.manufacturerNameEn()))
                .query(UUID.class).single();
    }

    private UUID upsertDosageForm(MedicinePackageWriteRequest request) {
        return jdbc.sql("""
                insert into dosage_form(id,code,name_ar,name_en,active,created_at,updated_at,version)
                values (:id,:code,:name_ar,:name_en,true,now(),now(),0)
                on conflict(code) do update set name_ar=excluded.name_ar,name_en=excluded.name_en,
                  active=true,updated_at=now(),version=dosage_form.version+1 returning id
                """).param("id", UUID.randomUUID()).param("code", upper(request.dosageFormCode()))
                .param("name_ar", clean(request.dosageFormNameAr())).param("name_en", clean(request.dosageFormNameEn()))
                .query(UUID.class).single();
    }

    private UUID upsertIngredient(IngredientInput ingredient) {
        return jdbc.sql("""
                insert into active_ingredient(id,code,name_ar,name_en,normalized_name,active,created_at,updated_at,version)
                values (:id,:code,:name_ar,:name_en,:normalized,true,now(),now(),0)
                on conflict(code) do update set name_ar=excluded.name_ar,name_en=excluded.name_en,
                  normalized_name=excluded.normalized_name,active=true,updated_at=now(),version=active_ingredient.version+1
                returning id
                """).param("id", UUID.randomUUID()).param("code", upper(ingredient.code()))
                .param("name_ar", clean(ingredient.nameAr())).param("name_en", clean(ingredient.nameEn()))
                .param("normalized", normalizer.normalize(ingredient.nameAr()) + " " + normalizer.normalize(ingredient.nameEn()))
                .query(UUID.class).single();
    }

    private void replaceIngredients(UUID medicineId, List<UUID> ids) {
        jdbc.sql("delete from medicine_active_ingredient where medicine_id=:id").param("id", medicineId).update();
        for (int i = 0; i < ids.size(); i++) {
            jdbc.sql("insert into medicine_active_ingredient(medicine_id,active_ingredient_id,sequence_number) values (:medicine,:ingredient,:sequence)")
                    .param("medicine", medicineId).param("ingredient", ids.get(i)).param("sequence", i + 1).update();
        }
    }

    private void replaceAliases(UUID medicineId, List<String> aliases) {
        jdbc.sql("delete from medicine_alias where medicine_id=:id").param("id", medicineId).update();
        Set<String> normalized = new HashSet<>();
        for (String alias : aliases) {
            String value = normalizer.normalize(alias);
            if (!value.isEmpty() && normalized.add(value)) {
                jdbc.sql("insert into medicine_alias(id,medicine_id,alias,normalized_alias,created_at) values (:id,:medicine,:alias,:normalized,now())")
                        .param("id", UUID.randomUUID()).param("medicine", medicineId)
                        .param("alias", clean(alias)).param("normalized", value).update();
            }
        }
    }

    private void refreshSearchText(UUID medicineId) {
        jdbc.sql("""
                update medicine_package mp set search_text=(
                  select trim(concat_ws(' ',m.normalized_name_ar,m.normalized_name_en,
                    mf.normalized_name,coalesce(ings.names,''),coalesce(aliases.names,''),lower(mp.strength_value::text),
                    lower(mp.strength_unit),lower(df.code),lower(df.name_ar),lower(df.name_en),
                    lower(mp.package_size_value::text),lower(mp.package_size_unit),lower(coalesce(mp.barcode,''))))
                  from medicine m join manufacturer mf on mf.id=m.manufacturer_id
                    join dosage_form df on df.id=mp.dosage_form_id
                    left join lateral (select string_agg(ai.normalized_name,' ') names
                      from medicine_active_ingredient mai join active_ingredient ai on ai.id=mai.active_ingredient_id
                      where mai.medicine_id=m.id) ings on true
                    left join lateral (select string_agg(ma.normalized_alias,' ') names
                      from medicine_alias ma where ma.medicine_id=m.id) aliases on true
                  where m.id=mp.medicine_id)
                where mp.medicine_id=:medicine
                """).param("medicine", medicineId).update();
    }

    private List<MedicinePackageResponse.IngredientResponse> ingredients(UUID packageId) {
        return jdbc.sql("""
                select ai.id,ai.code,ai.name_ar,ai.name_en from medicine_package mp
                join medicine_active_ingredient mai on mai.medicine_id=mp.medicine_id
                join active_ingredient ai on ai.id=mai.active_ingredient_id
                where mp.id=:id order by mai.sequence_number
                """).param("id", packageId).query((rs, row) -> new MedicinePackageResponse.IngredientResponse(
                rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("name_ar"), rs.getString("name_en"))).list();
    }

    private List<UUID> ingredientIds(UUID medicineId) {
        return jdbc.sql("select active_ingredient_id from medicine_active_ingredient where medicine_id=:id order by sequence_number")
                .param("id", medicineId).query(UUID.class).list();
    }

    private void validate(MedicinePackageWriteRequest request) {
        if (request.restricted() && clean(request.restrictionCode()).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESTRICTION_CODE_REQUIRED", "Restricted medicines require a restriction code.");
        }
        if ((request.officialPrice() == null) != (request.currency() == null || request.currency().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICE_CURRENCY", "Price and currency must be supplied together.");
        }
        Set<String> codes = new HashSet<>();
        if (request.activeIngredients().stream().map(i -> upper(i.code())).anyMatch(code -> !codes.add(code))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_ACTIVE_INGREDIENT", "Active ingredients must be unique.");
        }
    }

    private void history(UUID packageId, String oldStatus, String newStatus, Boolean oldActive, boolean newActive,
                         UUID actorId, String reason) {
        jdbc.sql("""
                insert into catalogue_status_history(id,medicine_package_id,old_status,new_status,old_active,new_active,
                  actor_user_id,reason,created_at) values (:id,:package,:old_status,:new_status,:old_active,:new_active,
                  case when exists(select 1 from app_user where id=:actor) then :actor else null end,:reason,now())
                """).param("id", UUID.randomUUID()).param("package", packageId).param("old_status", oldStatus)
                .param("new_status", newStatus).param("old_active", oldActive).param("new_active", newActive)
                .param("actor", actorId).param("reason", reason).update();
    }

    private void audit(UUID actorId, String type, UUID aggregateId, RequestMetadata metadata, String details) {
        jdbc.sql("""
                insert into audit_event(id,actor_user_id,event_type,aggregate_type,aggregate_id,outcome,correlation_id,
                  ip_address,user_agent,metadata,created_at)
                values (:id,case when exists(select 1 from app_user where id=:actor) then :actor else null end,
                  :type,'MEDICINE_PACKAGE',:aggregate,'SUCCESS',:correlation,:ip,:user_agent,:metadata,now())
                """).param("id", UUID.randomUUID()).param("actor", actorId).param("type", type)
                .param("aggregate", aggregateId).param("correlation", MDC.get("requestId"))
                .param("ip", metadata.ipAddress()).param("user_agent", metadata.userAgent()).param("metadata", details).update();
    }

    private List<UUID> replay(String key, String operation, String hash) {
        if (key == null || key.isBlank()) return null;
        requireIdempotencyKey(key);
        var row = jdbc.sql("select operation,request_hash,result_ids from catalogue_idempotency_key where idempotency_key=:key")
                .param("key", key).query((rs, n) -> new String[]{rs.getString(1), rs.getString(2), rs.getString(3)}).optional();
        if (row.isEmpty()) return null;
        if (!operation.equals(row.get()[0]) || !hash.equals(row.get()[1])) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "The idempotency key was already used for another request.");
        }
        return Arrays.stream(row.get()[2].split(",")).filter(value -> !value.isBlank()).map(UUID::fromString).toList();
    }

    private void remember(String key, String operation, String hash, List<UUID> ids) {
        if (key == null || key.isBlank()) return;
        jdbc.sql("insert into catalogue_idempotency_key(idempotency_key,operation,request_hash,result_ids,created_at) values (:key,:operation,:hash,:ids,now())")
                .param("key", key).param("operation", operation).param("hash", hash)
                .param("ids", String.join(",", ids.stream().map(UUID::toString).toList())).update();
    }

    private void requireIdempotencyKey(String key) {
        if (key == null || !key.matches("[A-Za-z0-9._-]{8,120}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY", "Provide an 8-120 character Idempotency-Key.");
        }
    }

    private ApiException duplicatePackage() {
        return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_MEDICINE_PACKAGE", "This exact medicine package or barcode already exists.");
    }

    private String unavailableReason(boolean packageActive, boolean medicineActive, boolean restricted, MedicinePackageStatus status) {
        if (!packageActive || !medicineActive) return "DISABLED";
        if (restricted) return "RESTRICTED";
        return status.name();
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String clean(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }
    private String nullable(String value) { String clean = clean(value); return clean.isEmpty() ? null : clean; }
    private String upper(String value) { return clean(value).toUpperCase(Locale.ROOT); }
}
