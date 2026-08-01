# Dawak API

Spring Boot modular-monolith API for Dawak Release 1.

## Local development

Prerequisites: Java 21 and Docker.

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The `local` profile writes OTP codes to the application log. This adapter is
strictly for local development. The default profile disables OTP delivery; a
production SMS implementation of `OtpDeliveryPort` must be configured before
deployment.

The local profile also loads an idempotent sample catalogue from
`db/local/sample_catalogue.sql`. It contains synthetic prices and availability
states for UI/API testing and is never loaded by the default or production
profiles. The sample includes available, unavailable, recalled, and unsupported
exact packages across Arabic and English names.

Production startup also requires strong, independently generated values for:

```text
DAWAK_OTP_PEPPER
DAWAK_JWT_SECRET
DAWAK_DATABASE_URL
DAWAK_DATABASE_USERNAME
DAWAK_DATABASE_PASSWORD
```

`DAWAK_JWT_SECRET` must contain at least 32 bytes. Secrets must not be committed.

## DAW-3 API

```text
POST   /api/v1/auth/otp/requests
POST   /api/v1/auth/otp/verifications
POST   /api/v1/auth/token/refresh
GET    /api/v1/auth/sessions
DELETE /api/v1/auth/sessions/{sessionId}
POST   /api/v1/auth/logout

GET    /api/v1/locations/cities
GET    /api/v1/locations/cities/{cityId}/areas

POST   /api/v1/patient/profile/complete
GET    /api/v1/patient/profile
PATCH  /api/v1/patient/profile
GET    /api/v1/patient/profile/consents

GET    /api/v1/medicines/search?q=&page=&size=
GET    /api/v1/medicines/{medicinePackageId}

POST   /api/v1/admin/catalogue/packages
PUT    /api/v1/admin/catalogue/packages/{medicinePackageId}
DELETE /api/v1/admin/catalogue/packages/{medicinePackageId}
POST   /api/v1/admin/catalogue/imports
```

Catalogue mutation and import endpoints require the `CATALOGUE_MANAGER` role.
Imports require an `Idempotency-Key` header; package creation accepts one for
safe client retries. Search supports Arabic normalization, English/Arabic names,
active ingredients, aliases, barcodes, dosage-form/manufacturer filters, and
pagination. Responses always identify one exact medicine package and expose a
`requestable` flag plus an `unavailableReason` when normal request flow is blocked.

OpenAPI UI is available at `/swagger-ui.html` while the application is running.

## HTTP and exception logging

Every HTTP exchange logs its request ID, method, path, response status, duration,
and sanitized JSON request/response bodies. The API accepts a safe `X-Request-ID`
header or generates one and returns it in the response. Credentials, OTP values,
phone numbers, and patient PII are redacted. Non-JSON and oversized payloads are
not written to logs.

```text
DAWAK_HTTP_LOG_BODIES=true
DAWAK_HTTP_LOG_MAX_PAYLOAD_LENGTH=8192
```

Set `DAWAK_HTTP_LOG_BODIES=false` when only HTTP metadata should be logged.
Expected API and validation failures are logged at WARN; unexpected exceptions
include a stack trace at ERROR and return a safe `INTERNAL_SERVER_ERROR` response.

## Verification

```bash
./gradlew test
```

The integration suite starts PostgreSQL 17 through Testcontainers and validates
Flyway migrations, OTP expiry/replay/attempt limits, rate limiting, registration,
policy consent, Arabic/English profile data, token rotation, device sessions,
logout, exact-package catalogue search, catalogue permissions and idempotency,
restriction gating, and audit persistence.
