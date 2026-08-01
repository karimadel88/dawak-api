# Dawak API

Spring Boot modular-monolith API for Dawak Release 1.

## Local development

Prerequisites: Java 21 and Docker.

```bash
docker compose up -d postgres minio clamav
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

Prescription binaries are placed in the private `dawak-prescriptions` MinIO
bucket as AES-GCM encrypted objects. MinIO's API is available on port 9000 and
its local console on <http://localhost:9001>. Quarantined uploads are scanned by
ClamAV on port 3310 before entering pharmacist review. The API fails closed when
storage or scanning is unavailable.

Production startup also requires strong, independently generated values for:

```text
DAWAK_OTP_PEPPER
DAWAK_JWT_SECRET
DAWAK_DATABASE_URL
DAWAK_DATABASE_USERNAME
DAWAK_DATABASE_PASSWORD
DAWAK_PRESCRIPTION_ENCRYPTION_SECRET
DAWAK_MINIO_ACCESS_KEY
DAWAK_MINIO_SECRET_KEY
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

Prescription upload logs identify the phase (`STORAGE_WRITE`, scan/finalization),
prescription ID, expected/received byte counts, state transition, and safe error
code. They never log file contents, upload/access tokens, or decrypted data.
Every error response includes a `requestId`; search for that value in the API
terminal to find the corresponding request log and exception stack trace.

Common prescription upload error codes:

```text
PRESCRIPTION_UPLOAD_TOKEN_INVALID    token expired, wrong, or already used
PRESCRIPTION_FILE_SIZE_MISMATCH      uploaded bytes differ from the intent
UNSUPPORTED_MEDIA_TYPE               PUT content was not application/octet-stream
PRESCRIPTION_STORAGE_UNAVAILABLE     MinIO write/read/delete failed
PRESCRIPTION_SCANNER_UNAVAILABLE     ClamAV is unreachable; finalization may be retried
PRESCRIPTION_CHECKSUM_MISMATCH       uploaded bytes do not match SHA-256
PRESCRIPTION_FILE_SIGNATURE_INVALID  MIME type does not match actual PDF/JPEG/PNG bytes
PRESCRIPTION_MALWARE_DETECTED        ClamAV rejected the object
```

Infrastructure logs can be inspected with:

```bash
docker-compose -f compose.yaml logs --tail=100 minio clamav
```

## DAW-5 prescription API

```text
POST /api/v1/prescriptions/upload-intents
PUT  /api/v1/prescriptions/{id}/content
POST /api/v1/prescriptions/{id}/finalize
GET  /api/v1/prescriptions/{id}
POST /api/v1/prescriptions/{id}/access-url
GET  /api/v1/prescriptions/{id}/content?accessToken=

GET  /api/v1/pharmacist/prescriptions/queue
POST /api/v1/pharmacist/prescriptions/{id}/claim
POST /api/v1/pharmacist/prescriptions/{id}/review
```

Upload and access tokens expire, are stored only as hashes, and are redacted
from HTTP logs. Only the patient or designated pharmacist can request an access
grant. Retention cleanup runs daily and removes expired encrypted objects and
access grants while preserving an anonymized audit record.

## DAW-6 medicine-request API

```text
POST /api/v1/medicine-requests
GET  /api/v1/medicine-requests
GET  /api/v1/medicine-requests/{id}
POST /api/v1/medicine-requests/{id}/submit
POST /api/v1/medicine-requests/{id}/qualify
POST /api/v1/medicine-requests/{id}/expand-radius
POST /api/v1/medicine-requests/{id}/cancel
```

Mutation endpoints require the `PATIENT` role and an `Idempotency-Key` header.
Creation accepts one exact active medicine package, quantity, city/area,
fulfillment preference, urgency, and an optional prescription. Submission checks
package restrictions and prescription approval, then matches only approved,
licensed pharmacies with active accepting branches in the requested service area.
Requests with no eligible branches become `UNFULFILLED` and can be retried with a
larger radius. Active requests expire automatically after the configured TTL.

Useful local request settings are `DAWAK_REQUEST_INITIAL_RADIUS_KM`,
`DAWAK_REQUEST_MAX_RADIUS_KM`, `DAWAK_REQUEST_MAX_QUANTITY`, and
`DAWAK_REQUEST_TTL`.

## Verification

```bash
./gradlew test
```

The integration suite starts PostgreSQL 17 through Testcontainers and validates
Flyway migrations, OTP expiry/replay/attempt limits, rate limiting, registration,
policy consent, Arabic/English profile data, token rotation, device sessions,
logout, exact-package catalogue search, catalogue permissions and idempotency,
restriction gating, and audit persistence.
