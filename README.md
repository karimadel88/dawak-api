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
```

OpenAPI UI is available at `/swagger-ui.html` while the application is running.

## Verification

```bash
./gradlew test
```

The integration suite starts PostgreSQL 17 through Testcontainers and validates
Flyway migrations, OTP expiry/replay/attempt limits, rate limiting, registration,
policy consent, Arabic/English profile data, token rotation, device sessions,
logout, and audit persistence.
