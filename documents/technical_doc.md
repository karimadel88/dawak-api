# Dawak — Technical Design Document

**Project name:** دواك — Dawak
**Document type:** Software Architecture and Technical Design
**Release:** MVP 1.0
**Status:** Proposed
**Backend platform:** Java and Spring Boot
**Architecture style:** Modular monolith
**Primary market:** Egypt
**Primary language:** Arabic, with English support

---

## 1. Document Purpose

This document defines the proposed technical architecture for Dawak, a medicine-access platform that connects patients with verified and licensed pharmacy branches.

The platform allows patients to submit medicine requests, upload prescriptions where required, receive availability offers from pharmacies, select an offer, and track fulfillment through pickup or delivery.

This document covers:

* System architecture
* Technology stack
* Backend modules
* Frontend applications
* Database design
* Authentication and authorization
* Medicine-request workflows
* Prescription handling
* Pharmacy matching
* API conventions
* Notifications
* Security
* Audit logging
* Deployment
* Testing
* Monitoring
* Implementation phases

---

## 2. System Objectives

The MVP must provide a secure and reliable way to complete the following workflow:

1. A patient searches for an exact medicine.
2. The patient creates a medicine request.
3. A prescription is uploaded when required.
4. The request is shared with eligible pharmacies.
5. Pharmacies submit availability offers.
6. The patient selects one offer.
7. The pharmacy confirms and prepares the order.
8. The patient collects the order or receives it through delivery.
9. The system records the complete transaction and audit history.

The platform must prevent unverified users from offering medicines and must not support customer-to-customer medicine sales.

---

## 3. Technical Principles

The following principles guide the MVP architecture.

### 3.1 Modular monolith first

The backend will be deployed as one Spring Boot application but divided into independent business modules.

This provides:

* Faster initial development
* Simpler transactions
* Easier deployment
* Lower infrastructure cost
* Clear business boundaries
* A future migration path to microservices

### 3.2 API-first development

Mobile and web applications will communicate with the backend through versioned REST APIs.

### 3.3 Secure by default

Medical documents, personal data, pharmacy information, and administrative actions must be protected from the first release.

### 3.4 Explicit business workflows

Order, offer, request, and prescription statuses must change through controlled domain operations rather than unrestricted database updates.

### 3.5 Auditable operations

Important actions must produce immutable audit records.

### 3.6 External integrations behind interfaces

SMS, object storage, push notifications, email, payment, and delivery integrations must be accessed through internal interfaces so providers can be replaced.

---

## 4. Proposed Technology Stack

### 4.1 Backend

| Technology        | Purpose                                                             |
| ----------------- | ------------------------------------------------------------------- |
| Java 21           | Main programming language                                           |
| Spring Boot       | Application framework                                               |
| Spring Web MVC    | REST API implementation                                             |
| Spring Security   | Authentication and authorization                                    |
| Spring Data JPA   | Persistence and repository layer                                    |
| Hibernate         | ORM implementation                                                  |
| PostgreSQL        | Primary relational database                                         |
| Redis             | Caching, temporary OTP data, locks, and background job coordination |
| Flyway            | Database migrations                                                 |
| Bean Validation   | Request and domain validation                                       |
| MapStruct         | DTO and entity mapping                                              |
| Springdoc OpenAPI | API documentation                                                   |
| Testcontainers    | Integration testing                                                 |
| JUnit 5           | Automated tests                                                     |
| Mockito           | Unit-test mocking                                                   |
| Maven             | Build and dependency management                                     |

Spring Boot 4.1.0 is the current stable release as of July 2026 and requires at least Java 17. Java 21 is an appropriate long-term-support baseline for the project. Spring Security 7.1 and Spring Data JPA 4.1 align with the current Spring Boot generation.

Before implementation begins, the team should confirm compatibility among the selected Spring Boot, Spring Security, Spring Data, Flyway, Hibernate, and third-party library versions.

### 4.2 Patient application

| Technology               | Purpose                        |
| ------------------------ | ------------------------------ |
| React Native             | Android and iOS application    |
| Expo                     | Mobile tooling and deployment  |
| TypeScript               | Type-safe frontend development |
| TanStack Query           | Server-state management        |
| Zustand                  | Local application state        |
| React Hook Form          | Form management                |
| Zod                      | Client-side validation         |
| Firebase Cloud Messaging | Push notifications             |

### 4.3 Pharmacy and administration portals

| Technology      | Purpose                         |
| --------------- | ------------------------------- |
| Next.js         | Web application framework       |
| TypeScript      | Type-safe development           |
| Tailwind CSS    | Styling                         |
| TanStack Query  | API state                       |
| React Hook Form | Forms                           |
| Zod             | Validation                      |
| Next-intl       | Arabic and English localization |

### 4.4 Infrastructure

| Technology                       | Purpose                                    |
| -------------------------------- | ------------------------------------------ |
| Docker                           | Application packaging                      |
| Docker Compose                   | Local development                          |
| GitHub Actions                   | Continuous integration and deployment      |
| Nginx or managed gateway         | Reverse proxy and TLS termination          |
| S3-compatible storage            | Prescription and pharmacy-document storage |
| Managed PostgreSQL               | Production database                        |
| Managed Redis                    | Cache and temporary state                  |
| Sentry                           | Application error monitoring               |
| OpenTelemetry                    | Metrics and distributed tracing            |
| Prometheus-compatible monitoring | Operational metrics                        |
| Grafana-compatible dashboards    | Monitoring visualization                   |

---

## 5. High-Level Architecture

```text
┌─────────────────────────┐
│ React Native Patient App│
└─────────────┬───────────┘
              │ HTTPS / REST
┌─────────────▼───────────┐
│ Next.js Pharmacy Portal │
└─────────────┬───────────┘
              │ HTTPS / REST
┌─────────────▼───────────┐
│ Next.js Admin Portal    │
└─────────────┬───────────┘
              │
       ┌──────▼───────┐
       │ API Gateway  │
       │ / Reverse    │
       │ Proxy        │
       └──────┬───────┘
              │
┌─────────────▼─────────────────────────────┐
│          Spring Boot Application          │
│                                           │
│ Auth │ Users │ Pharmacy │ Medicine        │
│ Prescription │ Requests │ Offers │ Orders │
│ Notifications │ Support │ Audit │ Reports │
└───────┬─────────────┬─────────────┬───────┘
        │             │             │
┌───────▼─────┐ ┌─────▼────┐ ┌──────▼──────┐
│ PostgreSQL  │ │ Redis    │ │ Object      │
│             │ │          │ │ Storage     │
└─────────────┘ └──────────┘ └─────────────┘
                      │
              ┌───────▼─────────┐
              │ External Systems│
              │ SMS / Push / Mail│
              │ Delivery / Pay   │
              └─────────────────┘
```

---

## 6. Backend Architecture

The backend will use a modular-monolith structure.

### 6.1 Recommended project structure

```text
com.dawak
├── DawakApplication.java
├── common
│   ├── api
│   ├── config
│   ├── domain
│   ├── exception
│   ├── persistence
│   ├── security
│   ├── storage
│   ├── validation
│   └── web
├── identity
├── patient
├── pharmacy
├── catalogue
├── prescription
├── request
├── offer
├── order
├── notification
├── support
├── compliance
├── audit
└── reporting
```

### 6.2 Internal module structure

Each business module should generally follow this structure:

```text
pharmacy
├── api
│   ├── PharmacyController.java
│   ├── PharmacyAdminController.java
│   └── dto
├── application
│   ├── PharmacyApplicationService.java
│   ├── PharmacyVerificationService.java
│   └── command
├── domain
│   ├── Pharmacy.java
│   ├── PharmacyBranch.java
│   ├── PharmacyStatus.java
│   ├── event
│   └── exception
├── infrastructure
│   ├── persistence
│   ├── mapper
│   └── integration
└── repository
    └── PharmacyRepository.java
```

### 6.3 Layer responsibilities

#### API layer

Responsible for:

* HTTP request handling
* Input validation
* Authentication context
* DTO conversion
* HTTP status codes
* OpenAPI annotations

The API layer must not contain business rules.

#### Application layer

Responsible for:

* Use-case orchestration
* Transaction boundaries
* Authorization checks
* Coordination among domain components
* Domain event publishing

#### Domain layer

Responsible for:

* Business rules
* Status transitions
* Domain entities
* Value objects
* Domain exceptions

#### Infrastructure layer

Responsible for:

* JPA implementation
* External integrations
* Storage providers
* Messaging
* Framework-specific adapters

---

## 7. Core Backend Modules

## 7.1 Identity and Access Module

Responsibilities:

* Patient registration
* Pharmacy-staff registration
* Administrator accounts
* OTP verification
* Authentication
* Refresh-token management
* Password management
* Session management
* Account locking
* Role and permission assignment

### Roles

```java
public enum SystemRole {
    PATIENT,
    PHARMACY_OWNER,
    PHARMACIST,
    PHARMACY_STAFF,
    SUPPORT_AGENT,
    COMPLIANCE_ADMIN,
    SYSTEM_ADMIN
}
```

### Authentication approach

The MVP will use:

* Phone number and OTP for patients
* Email or phone and password for pharmacy staff
* Password and MFA for administrators
* Short-lived JWT access tokens
* Rotating refresh tokens
* Server-side refresh-token records
* Device-session tracking

### Token recommendations

| Token                 | Recommended lifetime |
| --------------------- | -------------------: |
| OTP                   |            5 minutes |
| Access token          |        10–15 minutes |
| Patient refresh token |              30 days |
| Admin refresh token   |           8–12 hours |
| Password-reset token  |           15 minutes |

Refresh tokens must be stored as hashes, not plaintext.

---

## 7.2 Patient Module

Responsibilities:

* Patient profile
* Contact data
* Saved addresses
* Preferred language
* Preferred location
* Notification preferences
* Consent records
* Account deletion request

### Patient profile fields

```text
id
user_id
first_name
last_name
birth_year_optional
preferred_language
default_city_id
default_area_id
created_at
updated_at
```

Avoid collecting unnecessary medical or identity data during the MVP.

---

## 7.3 Pharmacy Module

Responsibilities:

* Pharmacy registration
* Pharmacy branches
* License documents
* Verification workflow
* Staff accounts
* Operating hours
* Delivery settings
* Service areas
* Pharmacy suspension

### Pharmacy status

```java
public enum PharmacyStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    SUSPENDED,
    DOCUMENTS_EXPIRED
}
```

### Branch status

```java
public enum BranchStatus {
    PENDING_APPROVAL,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
```

A pharmacy branch cannot receive requests unless:

* The pharmacy is approved
* The branch is active
* Required documents are valid
* The branch supports the request area
* The branch is currently eligible to receive requests

---

## 7.4 Medicine Catalogue Module

Responsibilities:

* Medicine records
* Arabic and English search
* Active ingredients
* Strengths
* Dosage forms
* Package sizes
* Manufacturers
* Barcodes
* Prescription requirements
* Restricted products
* Catalogue versioning

### Medicine model

A medicine must not be represented using only a free-text name.

```text
medicine_product
- id
- arabic_name
- english_name
- manufacturer_id
- active
- prescription_required
- restricted
- storage_type
- created_at
- updated_at

medicine_package
- id
- medicine_product_id
- strength
- strength_unit
- dosage_form_id
- package_size
- package_unit
- barcode
- official_price_optional
- active
```

### Search support

The search implementation should support:

* Arabic medicine name
* English medicine name
* Active ingredient
* Barcode
* Manufacturer
* Alternative spelling
* Partial matching

For the initial release, PostgreSQL full-text search and trigram indexes may be sufficient.

A dedicated search engine can be introduced later.

---

## 7.5 Prescription Module

Responsibilities:

* Prescription metadata
* Secure upload
* Review status
* Pharmacist review
* Rejection reasons
* Access logging
* File retention
* Secure download authorization

### Prescription status

```java
public enum PrescriptionStatus {
    UPLOADED,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    NEEDS_CLARIFICATION,
    EXPIRED
}
```

### Storage design

The actual prescription file must be stored in private object storage.

PostgreSQL stores only metadata:

```text
prescription
- id
- patient_id
- request_id
- storage_key
- original_filename
- content_type
- file_size
- checksum
- status
- reviewed_by
- reviewed_at
- rejection_reason
- retention_until
- created_at
```

### File access

Prescription files must be accessed through:

* An authenticated backend endpoint, or
* A short-lived signed object-storage URL

The backend must verify authorization before generating the URL.

### Prescription access log

```text
prescription_access_log
- id
- prescription_id
- accessed_by_user_id
- access_type
- ip_address
- user_agent
- created_at
```

---

## 7.6 Medicine Request Module

Responsibilities:

* Medicine-request creation
* Request validation
* Prescription association
* Location selection
* Pharmacy matching
* Request expiration
* Request cancellation
* Request history

### Request status

```java
public enum MedicineRequestStatus {
    DRAFT,
    SUBMITTED,
    AWAITING_PRESCRIPTION,
    PRESCRIPTION_REVIEW,
    READY_FOR_MATCHING,
    SENT_TO_PHARMACIES,
    OFFERS_RECEIVED,
    OFFER_SELECTED,
    UNFULFILLED,
    EXPIRED,
    CANCELLED,
    COMPLETED
}
```

### Request entity

```text
medicine_request
- id
- patient_id
- city_id
- area_id
- latitude_optional
- longitude_optional
- fulfillment_type
- urgency
- status
- prescription_id_optional
- expires_at
- created_at
- updated_at
- version
```

### Request item

```text
medicine_request_item
- id
- request_id
- medicine_package_id
- requested_quantity
- patient_notes_optional
```

The MVP may initially restrict each request to one medicine item to simplify pharmacy matching and offer comparison.

Multi-item requests can be introduced later.

---

## 7.7 Pharmacy Matching Module

Responsibilities:

* Identify eligible branches
* Record which branches received a request
* Avoid duplicate notifications
* Apply distance and service-area rules
* Expand the search radius where necessary

### Initial matching criteria

A pharmacy branch must satisfy:

* Approved pharmacy
* Active branch
* Same city
* Supported patient area
* Open or accepting asynchronous requests
* Prescription support where required
* Not suspended
* Within configured request radius
* Has not rejected the request already

### Match record

```text
request_pharmacy_match
- id
- request_id
- pharmacy_branch_id
- match_score
- match_reason
- notification_status
- viewed_at
- responded_at
- created_at
```

### Initial matching algorithm

```text
1. Find approved branches in the selected area.
2. Filter by fulfillment type.
3. Filter by prescription capability.
4. Sort by area match, service radius, operating state, and historical response rate.
5. Notify the first batch.
6. Expand to additional branches if no offer is received.
```

The MVP does not require machine-learning-based matching.

---

## 7.8 Offer Module

Responsibilities:

* Pharmacy availability responses
* Quantity
* Price
* Preparation time
* Delivery fee
* Expiration
* Offer selection
* Competing-offer rejection

### Offer status

```java
public enum OfferStatus {
    ACTIVE,
    SELECTED,
    NOT_SELECTED,
    EXPIRED,
    WITHDRAWN,
    CANCELLED
}
```

### Offer entity

```text
pharmacy_offer
- id
- request_id
- pharmacy_branch_id
- status
- medicine_package_id
- available_quantity
- unit_price
- delivery_fee
- preparation_minutes
- expires_at
- notes
- created_by_user_id
- created_at
- updated_at
- version
```

### Offer selection rules

* Only the request owner can select an offer.
* The request must remain open.
* The offer must be active.
* The offer must not have expired.
* The offered quantity must satisfy the selected quantity.
* Only one offer may be selected.
* Selection must create an order within the same transaction.
* Other active offers must become `NOT_SELECTED`.
* A stock reservation must be created.

### Concurrency protection

Offer selection must use:

* Optimistic locking through a version column, and
* A database transaction

For highly contested operations, a pessimistic lock can be used on the medicine request.

---

## 7.9 Order Module

Responsibilities:

* Convert selected offers into orders
* Order state management
* Pharmacy confirmation
* Preparation
* Pickup
* Delivery
* Cancellation
* Completion

### Order status

```java
public enum OrderStatus {
    PENDING_PHARMACY_CONFIRMATION,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    COLLECTED,
    DELIVERED,
    CANCELLED,
    FAILED
}
```

### Allowed state transitions

```text
PENDING_PHARMACY_CONFIRMATION
    → CONFIRMED
    → CANCELLED

CONFIRMED
    → PREPARING
    → CANCELLED

PREPARING
    → READY_FOR_PICKUP
    → OUT_FOR_DELIVERY
    → CANCELLED

READY_FOR_PICKUP
    → COLLECTED
    → CANCELLED

OUT_FOR_DELIVERY
    → DELIVERED
    → FAILED

FAILED
    → OUT_FOR_DELIVERY
    → CANCELLED
```

Status changes must be implemented through domain methods:

```java
public void confirm(UUID actingBranchId) {
    requireStatus(OrderStatus.PENDING_PHARMACY_CONFIRMATION);
    requireBranch(actingBranchId);

    this.status = OrderStatus.CONFIRMED;
    this.confirmedAt = Instant.now();
}
```

Controllers must never accept arbitrary order-status values.

---

## 7.10 Notification Module

Responsibilities:

* In-app notifications
* Push notifications
* SMS messages
* Email messages later
* Retry management
* Notification preferences
* Delivery status

### Notification types

```java
public enum NotificationType {
    OTP_CREATED,
    PHARMACY_APPROVED,
    PHARMACY_REJECTED,
    NEW_MEDICINE_REQUEST,
    NEW_OFFER_RECEIVED,
    OFFER_EXPIRING,
    PRESCRIPTION_APPROVED,
    PRESCRIPTION_REJECTED,
    ORDER_CONFIRMED,
    ORDER_READY,
    ORDER_OUT_FOR_DELIVERY,
    ORDER_COMPLETED,
    ORDER_CANCELLED
}
```

### Notification architecture

Business services publish internal domain events.

```text
OfferCreatedEvent
OrderConfirmedEvent
PrescriptionApprovedEvent
```

Event handlers create durable notification records.

External delivery should happen asynchronously.

The MVP may use:

* Spring application events
* A transactional outbox table
* A scheduled outbox processor

Kafka is not required for the initial version.

### Outbox table

```text
outbox_event
- id
- aggregate_type
- aggregate_id
- event_type
- payload_json
- status
- retry_count
- available_at
- created_at
- processed_at
```

This avoids losing notifications when database transactions succeed but external provider calls fail.

---

## 7.11 Support Module

Responsibilities:

* Support tickets
* Complaints
* Ticket messages
* Assignment
* Resolution
* Escalation
* Patient and pharmacy communication

### Ticket status

```java
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    WAITING_FOR_CUSTOMER,
    RESOLVED,
    CLOSED
}
```

### Ticket categories

* Medicine not found
* Pharmacy cancellation
* Wrong order
* Delivery issue
* Payment issue
* Prescription issue
* Privacy issue
* Suspected counterfeit
* Account issue
* Technical problem

Safety-related categories should have separate escalation rules.

---

## 7.12 Audit Module

Responsibilities:

* Record sensitive actions
* Preserve actor identity
* Record affected entity
* Store change metadata
* Support compliance investigations

### Audit event

```text
audit_log
- id
- actor_user_id
- actor_role
- action
- entity_type
- entity_id
- old_values_json_optional
- new_values_json_optional
- ip_address
- user_agent
- correlation_id
- created_at
```

### Actions requiring audit records

* Pharmacy approval or rejection
* Pharmacy suspension
* Prescription access
* Prescription approval or rejection
* Medicine restriction change
* Offer selection
* Order cancellation
* Manual status override
* User suspension
* Role assignment
* Data export
* Account deletion
* Administrative configuration change

Audit records must not be editable through the application.

---

## 8. Database Design

## 8.1 Main tables

```text
users
roles
user_roles
refresh_tokens
otp_challenges
patient_profiles
addresses
consent_records

pharmacies
pharmacy_branches
pharmacy_documents
pharmacy_staff
pharmacy_service_areas
pharmacy_operating_hours

manufacturers
active_ingredients
dosage_forms
medicine_products
medicine_packages
medicine_aliases

prescriptions
prescription_access_logs

medicine_requests
medicine_request_items
request_pharmacy_matches

pharmacy_offers
stock_reservations

orders
order_items
order_status_history
deliveries

notifications
notification_deliveries
device_tokens
outbox_events

support_tickets
support_messages

audit_logs
cities
areas
system_settings
```

## 8.2 Identifier strategy

Use UUID values for public and internal entity identifiers.

Recommended type:

```java
UUID
```

PostgreSQL can use native UUID columns.

### Benefits

* Difficult to enumerate
* Safe across distributed systems
* Suitable for offline or future service generation
* Avoids exposing sequential business volume

Sequential human-readable reference numbers may be generated separately:

```text
Request: REQ-2026-00001234
Order: ORD-2026-00005678
Ticket: TKT-2026-00000112
```

---

## 8.3 Timestamp strategy

Store timestamps in UTC using:

```java
Instant
```

Convert timestamps into the user’s local timezone at the presentation layer.

Required fields:

```text
created_at
updated_at
created_by
updated_by
```

Entities that support concurrency should also contain:

```text
version
```

---

## 8.4 Soft deletion

Soft deletion may be used for:

* Users
* Pharmacy staff
* Addresses
* Medicine aliases
* Non-sensitive content

Do not silently soft-delete:

* Orders
* Offers
* Prescriptions
* Audit logs
* Request history
* Financial records

These should use lifecycle statuses and retention policies.

---

## 8.5 Important indexes

Recommended indexes include:

```text
users(phone_number)
users(email)

pharmacy_branches(city_id, area_id, status)
pharmacy_service_areas(branch_id, area_id)

medicine_products(arabic_name)
medicine_products(english_name)
medicine_packages(barcode)
medicine_aliases(normalized_alias)

medicine_requests(patient_id, created_at)
medicine_requests(status, expires_at)
medicine_requests(city_id, area_id, status)

request_pharmacy_matches(pharmacy_branch_id, created_at)
request_pharmacy_matches(request_id, pharmacy_branch_id)

pharmacy_offers(request_id, status)
pharmacy_offers(pharmacy_branch_id, created_at)
pharmacy_offers(expires_at, status)

orders(patient_id, created_at)
orders(pharmacy_branch_id, status)
orders(status, created_at)

notifications(user_id, read_at, created_at)
outbox_events(status, available_at)
audit_logs(entity_type, entity_id)
audit_logs(actor_user_id, created_at)
```

---

## 9. REST API Design

## 9.1 Base URL

```text
/api/v1
```

## 9.2 Resource examples

```text
/api/v1/auth
/api/v1/patients
/api/v1/pharmacies
/api/v1/pharmacy-branches
/api/v1/medicines
/api/v1/prescriptions
/api/v1/medicine-requests
/api/v1/offers
/api/v1/orders
/api/v1/notifications
/api/v1/support-tickets
/api/v1/admin
```

## 9.3 Example endpoints

### Authentication

```text
POST /api/v1/auth/patient/request-otp
POST /api/v1/auth/patient/verify-otp
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/auth/sessions
DELETE /api/v1/auth/sessions/{sessionId}
```

### Medicines

```text
GET /api/v1/medicines/search?q=
GET /api/v1/medicines/{medicinePackageId}
GET /api/v1/medicines/{medicinePackageId}/alternatives
```

The alternatives endpoint must return catalogue relationships only. It must not automatically recommend medical substitution.

### Prescriptions

```text
POST /api/v1/prescriptions/upload-url
POST /api/v1/prescriptions
GET  /api/v1/prescriptions/{id}
POST /api/v1/prescriptions/{id}/approve
POST /api/v1/prescriptions/{id}/reject
POST /api/v1/prescriptions/{id}/clarification
```

### Medicine requests

```text
POST /api/v1/medicine-requests
GET  /api/v1/medicine-requests
GET  /api/v1/medicine-requests/{id}
POST /api/v1/medicine-requests/{id}/submit
POST /api/v1/medicine-requests/{id}/cancel
GET  /api/v1/medicine-requests/{id}/offers
```

### Pharmacy requests

```text
GET  /api/v1/pharmacy/requests
GET  /api/v1/pharmacy/requests/{id}
POST /api/v1/pharmacy/requests/{id}/offers
POST /api/v1/pharmacy/requests/{id}/reject
```

### Offers

```text
GET  /api/v1/offers/{id}
POST /api/v1/offers/{id}/select
POST /api/v1/offers/{id}/withdraw
```

### Orders

```text
GET  /api/v1/orders
GET  /api/v1/orders/{id}
POST /api/v1/orders/{id}/confirm
POST /api/v1/orders/{id}/start-preparation
POST /api/v1/orders/{id}/ready-for-pickup
POST /api/v1/orders/{id}/out-for-delivery
POST /api/v1/orders/{id}/collected
POST /api/v1/orders/{id}/delivered
POST /api/v1/orders/{id}/cancel
```

---

## 9.4 Request format

```json
{
  "medicinePackageId": "9ec2a270-b82d-4e0f-b349-88c81fbe8810",
  "quantity": 2,
  "cityId": "4897409c-f98f-49c3-8794-ce324be25793",
  "areaId": "db17ec82-9de0-42da-bc3e-cf02cb05204b",
  "fulfillmentType": "PICKUP",
  "urgency": "NORMAL",
  "prescriptionId": null,
  "notes": "Exact package requested"
}
```

## 9.5 Success-response format

```json
{
  "data": {
    "id": "f2a9c26d-5dcb-4671-b63e-46fdf861ee20",
    "referenceNumber": "REQ-2026-00001234",
    "status": "SUBMITTED"
  },
  "meta": {
    "timestamp": "2026-07-30T20:00:00Z",
    "correlationId": "b0ff16cb-3475-42f5-a180-ae8183a36f3c"
  }
}
```

## 9.6 Error-response format

Use RFC 9457-compatible problem details.

```json
{
  "type": "https://api.dawak.example/problems/offer-expired",
  "title": "Offer expired",
  "status": 409,
  "detail": "The selected pharmacy offer is no longer active.",
  "instance": "/api/v1/offers/123/select",
  "code": "OFFER_EXPIRED",
  "correlationId": "b0ff16cb-3475-42f5-a180-ae8183a36f3c",
  "timestamp": "2026-07-30T20:00:00Z"
}
```

---

## 10. Security Architecture

## 10.1 Authentication

Spring Security will manage authentication and authorization. It provides authentication, authorization, and protection against common web attacks.

### Patient authentication

* Phone number
* OTP
* Device session
* Access token
* Refresh token

### Pharmacy authentication

* Email or phone
* Password
* Optional OTP
* Branch membership
* Role assignment

### Administrator authentication

* Email and password
* Mandatory MFA
* Shorter session lifetime
* Restricted network rules where practical

---

## 10.2 Authorization

Use both URL-level and method-level authorization.

```java
@EnableMethodSecurity
@Configuration
public class MethodSecurityConfiguration {
}
```

Example:

```java
@PreAuthorize("""
    hasAnyRole('PHARMACIST', 'PHARMACY_OWNER')
    and @branchSecurity.canAccessBranch(authentication, #branchId)
    """)
public OfferResponse createOffer(
        UUID branchId,
        UUID requestId,
        CreateOfferCommand command
) {
    // implementation
}
```

Authorization must verify:

* Role
* Resource ownership
* Pharmacy-branch membership
* Pharmacy approval status
* Account status
* Request state
* Prescription permissions

A role check by itself is not enough.

---

## 10.3 File security

Prescription and pharmacy-document uploads must enforce:

* Allowed MIME types
* Maximum file size
* File signature validation
* Random storage keys
* Malware scanning
* Private storage buckets
* Encryption at rest
* Signed access URLs
* Short URL expiration
* Access audit records

Do not trust the filename extension or browser-provided content type.

---

## 10.4 API security

Required controls:

* HTTPS only
* CORS allowlist
* Rate limiting
* Request-size limits
* Secure headers
* JSON content-type enforcement
* Input validation
* SQL-injection protection
* Output encoding
* Brute-force protection
* OTP-attempt limits
* Refresh-token reuse detection
* Correlation IDs
* Sensitive-field redaction

---

## 10.5 Data protection

Sensitive data should be categorized.

### Highly sensitive

* Prescription images
* Patient medicine requests
* Patient phone numbers
* Pharmacy legal documents
* Authentication secrets

### Sensitive

* Addresses
* Delivery details
* Support complaints
* Audit information

### Operational

* Medicine catalogue
* Cities
* Public pharmacy information
* General platform content

Database-column encryption should be considered for particularly sensitive values, while all disks and object-storage buckets must use encryption at rest.

---

## 11. Main Transaction Workflows

## 11.1 Create medicine request

```text
Patient submits request
        ↓
Validate medicine package
        ↓
Validate quantity
        ↓
Check prescription requirement
        ↓
Persist request
        ↓
Set appropriate status
        ↓
Publish request-created event
```

### Transaction boundary

The request and its items must be committed in one transaction.

---

## 11.2 Submit request to pharmacies

```text
Request becomes READY_FOR_MATCHING
        ↓
Find eligible branches
        ↓
Create match records
        ↓
Write notification events to outbox
        ↓
Set request to SENT_TO_PHARMACIES
```

The matching transaction should not directly call SMS or push providers.

---

## 11.3 Select offer

This is one of the most important transactional operations.

```java
@Transactional
public OrderResponse selectOffer(
        UUID patientId,
        UUID offerId
) {
    PharmacyOffer offer = offerRepository.findByIdForUpdate(offerId)
        .orElseThrow(() -> new OfferNotFoundException(offerId));

    MedicineRequest request = offer.getRequest();

    request.assertOwnedBy(patientId);
    request.assertOfferCanBeSelected();
    offer.assertActive();
    offer.assertNotExpired(clock.instant());

    request.selectOffer(offer.getId());
    offer.markSelected();

    offerRepository.markCompetingOffersNotSelected(
        request.getId(),
        offer.getId()
    );

    StockReservation reservation =
        StockReservation.fromOffer(offer);

    Order order = Order.fromSelectedOffer(
        request,
        offer,
        reservation
    );

    reservationRepository.save(reservation);
    orderRepository.save(order);

    outboxRepository.save(
        OrderCreatedEvent.from(order)
    );

    return orderMapper.toResponse(order);
}
```

The operation must guarantee that two offers cannot be selected for the same request.

---

## 11.4 Pharmacy confirms order

```text
Pharmacy opens order
        ↓
Verify pharmacy branch ownership
        ↓
Verify order status
        ↓
Confirm stock availability
        ↓
Change status to CONFIRMED
        ↓
Write history
        ↓
Create patient notification
```

---

## 12. Concurrency and Transaction Management

Use `@Transactional` at application-service boundaries.

Important concurrent operations include:

* Offer selection
* Order confirmation
* Stock reservation
* Offer withdrawal
* Request cancellation
* Pharmacy response
* Notification-outbox processing

### Optimistic locking

Use `@Version` on:

* MedicineRequest
* PharmacyOffer
* Order
* StockReservation
* Pharmacy
* PharmacyBranch

```java
@Version
private long version;
```

### Pessimistic locking

Pessimistic locking may be used for offer selection:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    select o
    from PharmacyOffer o
    join fetch o.request r
    where o.id = :offerId
    """)
Optional<PharmacyOffer> findByIdForUpdate(UUID offerId);
```

Locks must be held for the shortest possible period.

---

## 13. Caching Strategy

Redis may be used for:

* OTP challenges
* Login-attempt counters
* Rate-limiting counters
* Medicine search suggestions
* City and area reference data
* Temporary request-matching data
* Distributed locks where required

Do not cache:

* Prescription content
* Raw authentication tokens
* Full patient medical-request histories
* Sensitive support-ticket content

Cache invalidation must happen when catalogue or location reference data changes.

---

## 14. Background Jobs

The MVP requires scheduled background processing for:

* Offer expiration
* Request expiration
* OTP cleanup
* Outbox-event processing
* Failed-notification retries
* Expired stock reservations
* Pharmacy-document expiry reminders
* Prescription-retention cleanup
* Daily operational reports

Spring Scheduler is sufficient initially.

Quartz may be introduced if jobs require persistence, complex schedules, or cluster coordination.

---

## 15. Localization

The platform will support:

* Arabic
* English
* Right-to-left layout
* Arabic medicine search
* Arabic validation messages
* Localized notification templates
* Localized status labels

Backend error codes must remain language-neutral.

Example:

```text
OFFER_EXPIRED
PRESCRIPTION_REQUIRED
PHARMACY_NOT_APPROVED
REQUEST_ALREADY_COMPLETED
```

The client selects the appropriate translated message.

---

## 16. Observability

## 16.1 Logging

Use structured JSON logs.

Every request should include:

* Correlation ID
* Authenticated user ID
* Role
* Request path
* HTTP method
* Response status
* Duration
* Service/module name

Do not log:

* JWT values
* Refresh tokens
* OTP values
* Passwords
* Prescription URLs
* Full prescription metadata
* Full patient notes
* Sensitive file contents

## 16.2 Metrics

Track:

* Request count
* Response time
* Error rate
* Database-pool usage
* Redis connectivity
* File-upload failures
* OTP failures
* Pharmacy matching duration
* Time to first offer
* Offer conversion
* Order completion
* Notification failures
* Outbox backlog

## 16.3 Health endpoints

Expose protected actuator endpoints for:

```text
/actuator/health
/actuator/metrics
/actuator/prometheus
```

Public health checks should disclose minimal information.

---

## 17. Testing Strategy

## 17.1 Unit tests

Test:

* Domain status transitions
* Price calculations
* Expiration logic
* Access rules
* Pharmacy matching
* Prescription requirements
* Cancellation rules

## 17.2 Integration tests

Use Testcontainers for:

* PostgreSQL
* Redis
* Object-storage emulator where practical

Integration tests should verify:

* Repository queries
* Database constraints
* Transactions
* Optimistic locking
* Flyway migrations
* Security filters
* API serialization

## 17.3 API tests

Test:

* Authentication
* Authorization
* Validation
* Correct HTTP statuses
* Error format
* Pagination
* Filtering
* File upload

## 17.4 Security tests

Test:

* Access across pharmacy branches
* Access to another patient’s request
* Unauthorized prescription access
* Expired token behavior
* Refresh-token replay
* OTP brute force
* Role escalation
* Malicious file uploads
* Rate limiting

## 17.5 End-to-end tests

Critical end-to-end scenario:

```text
Patient registers
→ searches medicine
→ creates request
→ uploads prescription
→ pharmacist approves
→ pharmacy receives request
→ pharmacy submits offer
→ patient selects offer
→ pharmacy confirms
→ pharmacy marks ready
→ patient collects
→ order completes
```

---

## 18. Deployment Architecture

## 18.1 Environments

Maintain separate environments:

* Local
* Development
* Testing
* Staging
* Production

Production data must not be copied into lower environments without anonymization.

## 18.2 Initial production deployment

```text
Internet
   │
Cloud Load Balancer / Nginx
   │
Spring Boot Containers
   │
   ├── Managed PostgreSQL
   ├── Managed Redis
   ├── Private Object Storage
   └── External Notification Providers
```

The initial release may use one or two backend instances.

Running at least two instances is preferable for availability.

## 18.3 Docker image

Use a multi-stage build:

```dockerfile
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
```

The final implementation should include JVM memory configuration, container health checks, and graceful shutdown.

---

## 19. CI/CD Pipeline

The GitHub Actions pipeline should perform:

```text
Checkout
→ Compile
→ Unit tests
→ Integration tests
→ Static analysis
→ Dependency vulnerability scan
→ Build Docker image
→ Push image
→ Deploy to staging
→ Run smoke tests
→ Manual production approval
→ Deploy production
→ Run production health checks
```

### Branch strategy

```text
main
develop
feature/*
release/*
hotfix/*
```

Alternatively, trunk-based development may be used with short-lived feature branches.

### Required checks

* Build succeeds
* Tests pass
* Migrations validate
* No critical dependency vulnerabilities
* Formatting and static analysis pass
* Docker image builds
* OpenAPI contract validates

---

## 20. Database Migration Strategy

Use Flyway.

Migration naming:

```text
V1__create_identity_tables.sql
V2__create_pharmacy_tables.sql
V3__create_catalogue_tables.sql
V4__create_prescription_tables.sql
V5__create_request_and_offer_tables.sql
V6__create_order_tables.sql
V7__create_notification_tables.sql
```

Rules:

* Never modify an applied production migration.
* Add a new migration for every change.
* Test upgrades from the previous production schema.
* Avoid long-running blocking migrations.
* Back up the database before high-risk migrations.
* Use expand-and-contract changes for zero-downtime releases.

---

## 21. Backup and Disaster Recovery

### PostgreSQL

* Automated daily backups
* Point-in-time recovery
* Encrypted backup storage
* Regular restoration tests
* Separate backup account or project

### Object storage

* Versioning
* Encryption
* Restricted deletion permissions
* Lifecycle policies
* Retention configuration

### Recovery targets

Initial recommended targets:

| Objective                |             Target |
| ------------------------ | -----------------: |
| Recovery point objective | 15 minutes or less |
| Recovery time objective  |    4 hours or less |

Final targets depend on infrastructure cost and business requirements.

---

## 22. Performance Requirements

Initial MVP performance targets:

| Operation                  |                            Target |
| -------------------------- | --------------------------------: |
| Standard API response      |               Under 500 ms at p95 |
| Medicine search            |             Under 1 second at p95 |
| Request submission         | Under 2 seconds, excluding upload |
| Offer selection            |                    Under 1 second |
| Pharmacy request list      |                    Under 1 second |
| Notification creation      |                      Under 500 ms |
| File-upload URL generation |                      Under 500 ms |

The system should initially support:

* Thousands of registered patients
* Hundreds of pharmacy branches
* Hundreds of concurrent users
* Tens of thousands of medicine catalogue records
* Gradual horizontal backend scaling

---

## 23. Technical Risks

| Risk                         | Mitigation                                    |
| ---------------------------- | --------------------------------------------- |
| Duplicate offer selection    | Transaction and database locking              |
| Expired availability         | Short offer expiration and reservation period |
| Unauthorized pharmacy access | Branch-level authorization                    |
| Prescription exposure        | Private storage and signed URLs               |
| Notification loss            | Transactional outbox                          |
| Incorrect catalogue match    | Structured medicine-package model             |
| Slow Arabic search           | Normalized fields and trigram indexes         |
| Database bottleneck          | Indexing, monitoring, query review            |
| Provider outage              | Retry, circuit breaker, provider abstraction  |
| Uploaded malware             | File validation and malware scanning          |
| Sensitive logs               | Central redaction policy                      |
| Fraudulent pharmacy account  | Manual verification and compliance review     |

---

## 24. MVP Implementation Plan

## Phase 1 — Foundation

Deliver:

* Spring Boot project
* Module structure
* PostgreSQL
* Flyway
* Redis
* Global error handling
* OpenAPI
* Docker
* CI pipeline
* Authentication foundation
* Audit infrastructure

## Phase 2 — Identity and pharmacy onboarding

Deliver:

* Patient OTP registration
* Pharmacy user authentication
* Pharmacy onboarding
* Branch creation
* Document upload
* Administrator review
* Pharmacy approval and suspension
* Role and branch permissions

## Phase 3 — Medicine catalogue

Deliver:

* Medicine products
* Medicine packages
* Arabic and English search
* Active ingredients
* Dosage forms
* Catalogue administration
* CSV import
* Prescription-required flag

## Phase 4 — Prescriptions and requests

Deliver:

* Secure prescription upload
* Prescription review
* Medicine-request creation
* Request validation
* Request lifecycle
* Patient request history

## Phase 5 — Pharmacy matching and offers

Deliver:

* Pharmacy matching
* Request inbox
* Pharmacy response
* Offer creation
* Offer expiration
* Offer comparison
* Patient offer selection

## Phase 6 — Orders

Deliver:

* Stock reservation
* Order creation
* Pharmacy confirmation
* Preparation status
* Pickup fulfillment
* Basic delivery status
* Cancellation
* Status history

## Phase 7 — Notifications and support

Deliver:

* Outbox
* Push notifications
* SMS notifications
* Notification inbox
* Support tickets
* Complaint escalation

## Phase 8 — Administration and reporting

Deliver:

* Admin dashboard
* Operational KPIs
* Pharmacy performance
* Request monitoring
* Order monitoring
* Audit-log search
* Configuration management

## Phase 9 — Hardening and pilot

Deliver:

* Security testing
* Performance testing
* Recovery testing
* Monitoring dashboards
* Pharmacy acceptance testing
* Patient acceptance testing
* Pilot deployment
* Incident response process

---

## 25. MVP Definition of Done

The MVP is technically ready for pilot when:

* All public endpoints use HTTPS.
* Patients can authenticate using OTP.
* Pharmacy accounts require approval.
* Branch-level permissions are enforced.
* The medicine catalogue distinguishes medicine, strength, form, and package.
* Prescriptions are stored privately.
* Prescription access is audited.
* Patients can submit medicine requests.
* Eligible pharmacies receive matched requests.
* Pharmacies can submit offers.
* Patients can select exactly one active offer.
* Offer selection creates one order transactionally.
* Pharmacies can complete pickup fulfillment.
* Notifications use a durable outbox.
* Critical actions create audit records.
* Database backups are configured.
* Monitoring and alerting are enabled.
* Critical security tests pass.
* A complete end-to-end pilot scenario succeeds.

---

## 26. Future Architecture Evolution

The modular monolith should remain in place until there is a proven need to separate services.

Possible future services include:

* Notification service
* Search service
* Pharmacy inventory service
* Payment service
* Delivery integration service
* Analytics service
* Prescription-verification service

Kafka or another event broker may be introduced when:

* Multiple services consume the same events
* Notification volume becomes high
* Pharmacy inventory updates become continuous
* Analytics requires real-time event streams
* Independent deployment becomes necessary

The first service extraction should be based on operational evidence, not architectural preference.

---

## 27. Final Technical Recommendation

Build Dawak as:

* Java 21 backend
* Spring Boot modular monolith
* Spring Security authentication and authorization
* Spring Data JPA and PostgreSQL
* Redis for OTP, caching, and temporary state
* Private S3-compatible object storage
* React Native patient application
* Next.js pharmacy and administration portal
* REST APIs
* Transactional outbox for asynchronous notifications
* Docker deployment
* GitHub Actions CI/CD
* Structured logging and centralized monitoring

The first technical objective is to deliver one secure and transactional end-to-end workflow:

> A patient submits an exact medicine request, a verified pharmacy responds with an offer, the patient selects it, an order is created once, and the pharmacy completes fulfillment with a complete audit trail.
