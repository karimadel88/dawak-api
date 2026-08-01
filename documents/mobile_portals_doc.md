# Dawak Mobile Application and Web Portals Requirements Document

**Project:** Ø¯ÙˆØ§Ùƒ â€” Dawak
**Document type:** Application and Portal Requirements
**Release:** MVP 1.1 â€” aligned Release 1 baseline with Release 2 extension boundary
**Backend:** Java 21 and Spring Boot
**Mobile application:** React Native
**Web portals:** Next.js
**Primary language:** Arabic
**Secondary language:** English
**Target market:** Egypt

---

# 1. Document Purpose

This document defines the functional, technical, design, security, and integration requirements for the Dawak client applications.

The project includes four Release 1 user-facing systems:

1. Patient mobile application
2. Public website
3. Pharmacy web portal
4. Administration web portal

The systems connect to a shared Spring Boot backend through secure REST APIs.

The primary MVP journey is:

> A patient searches for a medicine, submits a request, receives offers from verified pharmacies, selects an offer, and tracks the order until pickup or delivery.

## 1.1 Approved release boundary

Section 16 of the approved Dawak business document is the authority for release
allocation.

Release 1 includes patient OTP, exact-package search, requests, secure
prescriptions, pharmacy matching/offers, pickup, basic pharmacy-managed
delivery status, notifications, availability alerts, support, safety incidents,
public content, pharmacy onboarding, administration, audit, and pilot reports.

Release 2 includes online payments, pharmacy subscriptions, live delivery
tracking, saved medicines, refill reminders, family/caregiver profiles,
charity-assistance cases, pharmacy Excel stock import, enhanced fraud controls,
and masked in-app chat.

Release 1 screens and navigation must not expose Release 2 actions, placeholders
that look functional, or endpoints. Approved substitution, customer medicine
listings/trading/donations, POS/ERP integrations, insurance, AI diagnosis, and
other future capabilities remain excluded.

---

# 2. Applications in Scope

## 2.1 Patient mobile application

The patient application allows users to:

* Register and authenticate
* Search the medicine catalogue
* Create medicine requests
* Upload prescriptions
* Receive pharmacy offers
* Select an offer
* Track orders
* Receive notifications
* Contact customer support

## 2.2 Public website

The public website allows visitors to:

* Learn how Dawak works and understand that it is an intermediary
* View supported cities and areas
* Search/view safe public catalogue information
* Read FAQs, policies, health-awareness content, notices, and safety announcements
* Start mobile-app installation or patient registration through approved links

It is not a transactional patient web application in Release 1 and does not
display private pharmacy availability or patient information.

## 2.3 Pharmacy web portal

The pharmacy portal allows approved pharmacy users to:

* Register and submit pharmacy information
* Manage branches and staff
* Receive medicine requests
* Perform authorized branch-level prescription fulfillment checks
* Submit offers
* Confirm and prepare orders
* Manage pickup or delivery
* Review pharmacy performance

## 2.4 Administration web portal

The administration portal allows authorized platform staff to:

* Verify pharmacies
* Manage medicine catalogue data
* Monitor requests, offers, and orders
* Review complaints and incidents
* Manage users and access
* Review audit logs
* Configure cities, areas, and system settings
* View operational reports

---

# 3. Recommended Technology Stack

## 3.1 Patient mobile application

| Technology                      | Purpose                              |
| ------------------------------- | ------------------------------------ |
| React Native                    | Android and iOS development          |
| Expo                            | Mobile build and development tooling |
| TypeScript                      | Type safety                          |
| Expo Router                     | Navigation                           |
| TanStack Query                  | API data and caching                 |
| Zustand                         | Local state management               |
| React Hook Form                 | Form handling                        |
| Zod                             | Client-side validation               |
| Axios                           | HTTP client                          |
| Expo SecureStore                | Secure token storage                 |
| Expo Notifications              | Push notification handling           |
| Firebase Cloud Messaging        | Android push notifications           |
| Apple Push Notification Service | iOS notifications                    |
| i18next or Expo Localization    | Arabic and English localization      |
| Sentry                          | Error monitoring                     |

## 3.2 Public website and web portals

| Technology                  | Purpose                   |
| --------------------------- | ------------------------- |
| Next.js                     | Web application framework |
| TypeScript                  | Type safety               |
| Tailwind CSS                | Styling                   |
| TanStack Query              | API state                 |
| React Hook Form             | Form handling             |
| Zod                         | Validation                |
| Axios                       | HTTP communication        |
| Next-intl                   | Localization              |
| Recharts or similar library | Dashboard charts          |
| Sentry                      | Error monitoring          |

## 3.3 Shared frontend packages

The mobile application and web portals should share where practical:

* API types
* Validation schemas
* Status definitions
* Translation keys
* Business constants
* Utility functions
* Error-code mappings

OpenAPI is the source of truth for transport DTOs, endpoints, statuses, error
codes, pagination, and validation limits. The frontend workspace generates a
versioned TypeScript API client during CI; it must not maintain handwritten
copies that compete with the backend contract.

Mobile and web applications may share design tokens, icons, translations,
status definitions, validation rules, and non-visual utilities. They should not
attempt to share complete React Native and DOM UI component implementations.

A shared package may be created using a monorepo.

```text
apps/
â”œâ”€â”€ patient-mobile
â”œâ”€â”€ public-web
â”œâ”€â”€ pharmacy-portal
â””â”€â”€ admin-portal

packages/
â”œâ”€â”€ api-client
â”œâ”€â”€ shared-types
â”œâ”€â”€ validation
â”œâ”€â”€ translations
â””â”€â”€ business-constants
```

---

# 4. General User Experience Requirements

All applications must provide:

* Arabic-first experience
* Complete right-to-left layout
* English-language option
* Responsive layouts
* Clear loading states
* Empty states
* Error states
* Confirmation messages
* Accessible form labels
* Consistent status colors and labels
* Clear success and failure feedback
* Protection against duplicate submissions
* Confirmation before destructive actions

The design must avoid presenting pharmaceutical information as medical advice.

---

# 5. Patient Mobile Application

# 5.1 Target Platforms

The first mobile release should support:

* Android
* iOS

Android should be treated as the primary pilot platform because it is expected to represent the larger initial user base.

The transactional patient journey remains mobile-first. A separate lightweight
Next.js public website is required for the first pilot, as defined in Section
2.2; it does not duplicate authenticated request/order screens.

---

# 5.2 Patient Roles

The MVP supports one mobile role:

```text
PATIENT
```

Release 2 may add:

* Caregiver
* Family account manager
* Corporate healthcare member
* Charity assistance applicant

---

# 5.3 Mobile Application Navigation

Recommended bottom navigation:

1. Home
2. My Requests
3. Orders
4. Notifications
5. Profile

Medicine search should be available prominently from the home screen.

---

# 5.4 Mobile Screens

## 5.4.1 Splash screen

Purpose:

* Display application branding
* Check authentication state
* Load minimum configuration
* Select the next screen

Requirements:

* Do not remain visible longer than necessary
* Handle expired sessions
* Handle service-maintenance status
* Support deep links from notifications

---

## 5.4.2 Language selection

Fields:

* Arabic
* English

Requirements:

* Arabic selected by default
* Save selection locally and on the user profile
* Change application direction dynamically
* Persist selection after logout where appropriate

---

## 5.4.3 Onboarding screens

Suggested onboarding messages:

* Find medicine from verified pharmacies
* Submit one request instead of contacting many pharmacies
* Upload prescriptions securely
* Track pickup and delivery

Requirements:

* Maximum three or four screens
* Skip option
* Do not show again after completion unless reset

---

## 5.4.4 Phone number login

Fields:

* Country code
* Phone number

Actions:

* Continue
* View terms and privacy policy

Validation:

* Valid Egyptian mobile-number format
* Rate-limit OTP requests
* Disable repeated taps while processing

---

## 5.4.5 OTP verification

Fields:

* Six-digit OTP

Actions:

* Verify
* Resend code
* Change phone number

Requirements:

* Display OTP expiration
* Limit invalid attempts
* Support automatic OTP fill where available
* Hide full phone number
* Redirect locked accounts to support

---

## 5.4.6 Profile completion

Fields:

* First name
* Last name
* Preferred language
* City
* Area

Optional fields:

* Email
* Birth year

Requirements:

* Collect only minimum data
* Display privacy explanation
* Require acceptance of current terms

---

## 5.4.7 Home screen

Main components:

* Medicine search bar
* Create request action
* Active request card
* Active order card
* Recent searches
* Availability-alert summary
* Supported services
* Help section

The home screen must emphasize the main action:

> Search for your medicine.

---

## 5.4.8 Medicine search

Search inputs:

* Arabic medicine name
* English medicine name
* Active ingredient
* Barcode where supported

Filters:

* Dosage form
* Strength
* Manufacturer
* Prescription requirement

Search result information:

* Arabic name
* English name
* Active ingredient
* Strength
* Dosage form
* Package size
* Manufacturer
* Prescription-required indicator

Requirements:

* Debounced search
* Loading skeleton
* No-results state
* Search-history storage
* Typo tolerance
* Arabic text normalization
* Do not show unverified user-created medicine names

---

## 5.4.9 Medicine details

Display:

* Arabic and English name
* Active ingredient
* Strength
* Form
* Package size
* Manufacturer
* Prescription requirement
* Storage note where relevant
* Safety disclaimer

Actions:

* Request this medicine
* Enable availability reminder

Saved medicines and refill reminders are Release 2 and must not appear in the
Release 1 build.

The screen must not automatically recommend substitutes.

---

## 5.4.10 Create medicine request

Fields:

* Medicine package
* Requested quantity
* City
* Area
* Pickup or delivery preference
* Urgency
* Patient notes
* Prescription attachment when required

Suggested urgency options:

* Normal
* Needed today
* Needed within two days

The application must clarify that urgency does not guarantee availability.

Validation:

* Quantity greater than zero
* Required location
* Required prescription when applicable
* Supported service area
* Maximum configured quantity

---

## 5.4.11 Prescription upload

Supported sources:

* Camera
* Photo library
* PDF file

Requirements:

* Show image preview
* Allow removal and replacement
* Display allowed formats
* Display maximum file size
* Compress large images safely
* Preserve readable resolution
* Upload directly using a signed URL
* Display upload progress
* Retry failed uploads
* Do not save files in an unprotected public directory
* Remove temporary local files where practical

User guidance should request:

* Clear image
* Full prescription visible
* Doctor details visible where applicable
* No blurred or cropped content

---

## 5.4.12 Request review

Before submission, display:

* Exact medicine
* Strength
* Dosage form
* Package size
* Quantity
* Location
* Fulfillment preference
* Prescription status
* Notes

Actions:

* Submit request
* Edit request

The submit action must be protected against duplicate requests caused by repeated taps.

---

## 5.4.13 Request success screen

Display:

* Request reference number
* Current status
* Estimated next step
* Notification information

Actions:

* View request
* Return home

Do not promise that a pharmacy will respond within a fixed period unless an operational SLA is formally supported.

---

## 5.4.14 My Requests screen

Tabs:

* Active
* Completed
* Cancelled

Each request card shows:

* Reference number
* Medicine name
* Quantity
* Date
* Status
* Number of offers
* Expiration time where applicable

Filters:

* Status
* Date
* Medicine

---

## 5.4.15 Request details

Display:

* Medicine details
* Requested quantity
* Location
* Prescription status
* Timeline
* Offers received
* Expiration time
* Cancellation action
* Support action

Possible timeline steps:

```text
Submitted
Prescription review
Sent to pharmacies
Offers received
Offer selected
Completed
```

When no active offer exists after the configured response window, the request
details screen may offer:

* Expand matching within supported areas
* Enable an exact-package availability alert
* Pause or cancel the alert

The screen must not offer automatic substitution or charity referral in
Release 1.

---

## 5.4.16 Offers list

Each pharmacy offer must display:

* Verified pharmacy name
* Branch name
* Area
* Available quantity
* Unit price
* Total price
* Delivery fee
* Total payable amount
* Preparation time
* Pickup or delivery
* Offer expiration
* Pharmacy notes

Sort options:

* Lowest total
* Fastest preparation
* Nearest branch
* Newest offer

The default sort should be transparent and not misleading.

---

## 5.4.17 Offer details

Display:

* Full pharmacy information
* Medicine and package
* Quantity
* Price breakdown
* Fulfillment method
* Preparation estimate
* Offer expiration
* Terms and cancellation information

Actions:

* Select offer
* Return to offers
* Report concern

A final confirmation modal should be displayed before selection.

---

## 5.4.18 Order details

Display:

* Order reference
* Selected pharmacy
* Branch address
* Medicine
* Quantity
* Price
* Delivery fee
* Fulfillment type
* Order status
* Status timeline
* Pharmacy contact through controlled channel
* Support action

Actions vary by status:

* Cancel order
* View pickup instructions
* Confirm collection
* Track delivery
* Report problem

---

## 5.4.19 Order tracking

Pickup statuses:

```text
Pending confirmation
Confirmed
Preparing
Ready for pickup
Collected
```

Delivery statuses:

```text
Pending confirmation
Confirmed
Preparing
Out for delivery
Delivered
```

Requirements:

* Refresh automatically when opened
* Support push-notification updates
* Do not expose delivery-agent personal information unnecessarily
* Show cancellation reason when relevant

---

## 5.4.20 Notifications screen

Notification types:

* Prescription status
* New offer
* Offer expiration
* Order confirmation
* Order preparation
* Ready for pickup
* Out for delivery
* Order completion
* Request expiration
* Support reply

Actions:

* Mark as read
* Mark all as read
* Open related request or order

Medical details should not appear in lock-screen push messages.

Example safe push message:

> You received a new pharmacy offer.

Avoid:

> Pharmacy X has your diabetes medicine available.

---

## 5.4.21 Support screen

Functions:

* Create support ticket
* Select category
* Add description
* Attach evidence
* View ticket status
* Reply to support
* Close resolved ticket

Categories:

* Request issue
* Pharmacy issue
* Order issue
* Delivery issue
* Prescription issue
* Privacy concern
* Technical issue
* Other

Safety-related incidents must be clearly separated and escalated.

The patient can report a safety incident linked to an order or request:

* Wrong medicine or strength
* Damaged package
* Suspected counterfeit
* Missing product
* Storage or delivery-temperature concern
* Privacy concern
* Adverse reaction

The incident flow captures severity guidance and evidence, displays approved
emergency/adverse-reaction escalation instructions, and does not treat those
events only as ordinary support tickets.

---

## 5.4.22 Profile screen

Sections:

* Personal information
* Saved addresses
* Language
* Notification preferences
* Active sessions
* Privacy settings
* Terms and policies
* Support
* Logout
* Delete account

---

# 6. Mobile Local State

## 6.1 Secure state

Store using secure storage:

* Refresh token
* Device-session identifier

Keep the short-lived access token in memory where practical. If platform
process restoration requires persistence, use secure storage with the same
logout/revocation controls and document that exception.

Do not store:

* Prescription files
* OTP values
* Full medical-request responses
* Passwords
* Sensitive support attachments

## 6.2 Normal local storage

May store:

* Language
* Completed onboarding flag
* Search history
* Non-sensitive interface preferences

## 6.3 Server state

Use TanStack Query for:

* Profile
* Medicine search
* Requests
* Offers
* Orders
* Notifications
* Support tickets

Recommended behavior:

* Cache short-lived list data
* Invalidate after mutations
* Retry safe GET operations
* Avoid automatically retrying non-idempotent POST operations unless an idempotency key is used

---

# 7. Mobile API Requirements

The mobile application requires the following backend API groups:

```text
/auth
/profile
/locations
/medicines
/prescriptions
/medicine-requests
/offers
/orders
/notifications
/support-tickets
/device-tokens
```

All request-creating endpoints should support idempotency.

Example header:

```text
Idempotency-Key: 1f417f39-8be0-49e2-a0dc-f4cc50344423
```

Prescription and pharmacy-document uploads use a quarantine/finalization flow:

```text
Create upload session
â†’ upload to signed private quarantine URL
â†’ confirm completion
â†’ wait for server validation and malware scan
â†’ attach only the clean finalized file
```

Clients must not display a quarantined or failed object as an accepted
prescription/document.

---

# 8. Mobile Notification Requirements

The application must:

* Register the device token after authentication
* Update the token when changed
* Remove token association on logout
* Handle notification deep links
* Support foreground notifications
* Support background notifications
* Handle revoked notification permission
* Provide in-app notification history

Push notifications should be used for convenience, not as the only source of truth. The app must fetch current status from the backend.

---

# 9. Mobile Offline and Network Handling

The MVP does not require full offline operation.

It must still handle:

* Slow connection
* Request timeout
* Lost connection
* Retry option
* Upload interruption
* Duplicate submission prevention
* Cached previously loaded screens
* Session expiration

A visible offline banner should be shown when connectivity is unavailable.

---

# 9.1 Public Website

The Release 1 public website is a separately deployed Next.js application.

Required pages:

* Home and Dawak value proposition
* How the service works
* Supported cities and areas
* Public medicine catalogue search/details
* FAQs
* Health-awareness articles
* Platform and safety notices
* Terms and Privacy Policy
* Mobile-app installation/start links

Requirements:

* Arabic-first RTL and English support
* Responsive and accessible layouts
* Search-engine-friendly public metadata
* Only published/current content from the backend content API
* No patient, prescription, private availability, or restricted operational data
* Clear intermediary, safety, and no-customer-trading statements
* Versioned policy links compatible with patient consent records

The public website may share translations, API client, validation, status
glossary, and design tokens with the portals. It must not share an
administrator or pharmacy session.

---

# 10. Pharmacy Web Portal

# 10.1 Pharmacy Portal Users

Supported roles:

```text
PHARMACY_OWNER
PHARMACIST
PHARMACY_STAFF
```

Permissions should be assigned per branch.

A pharmacy employee must not automatically access all branches.

---

# 10.2 Pharmacy Portal Navigation

Recommended sidebar:

1. Dashboard
2. Medicine Requests
3. Offers
4. Orders
5. Branches
6. Staff
7. Documents
8. Notifications
9. Reports
10. Settings
11. Support

Navigation items should be hidden when the user lacks permission.

---

# 10.3 Pharmacy Authentication

Functions:

* Login
* Password recovery
* OTP or MFA where configured
* Session management
* Logout
* Password change

Security controls:

* Login-attempt limits
* Account lock
* Session expiration
* Device and IP information
* Forced logout after suspension
* Strong-password policy

Portal sessions use `HttpOnly`, `Secure`, and appropriate `SameSite` cookies
with CSRF protection. Tokens must never be stored in `localStorage`.
Pharmacy owners and users permitted to view prescriptions require MFA or the
approved step-up authentication control.

---

# 10.4 Pharmacy Onboarding

## Business information

Fields:

* Legal pharmacy name
* Public pharmacy name
* License number
* License expiry
* Responsible pharmacist
* Tax information where required
* Commercial registration where required
* Contact phone
* Contact email

## Branch information

Fields:

* Branch name
* City
* Area
* Full address
* Map coordinates
* Opening hours
* Pickup support
* Delivery support
* Delivery areas
* Delivery fee settings
* Prescription handling capability

## Documents

Possible documents:

* Pharmacy license
* Responsible pharmacist documentation
* Tax documentation
* Commercial documentation
* Identity documentation for authorized representative

Requirements:

* Upload progress
* File validation
* Document status
* Rejection reason
* Expiry alerts
* Resubmission flow

---

# 10.5 Pharmacy Approval States

Display clear onboarding status:

```text
Draft
Submitted
Under review
Approved
Rejected
Suspended
Documents expired
```

The portal should disable business operations until approval.

---

# 10.6 Pharmacy Dashboard

Display:

* New requests
* Requests awaiting response
* Active offers
* Selected offers
* Orders requiring confirmation
* Orders being prepared
* Orders ready for pickup
* Deliveries in progress
* Expiring offers
* Recent cancellations

Operational metrics:

* Response rate
* Average response time
* Offer-selection rate
* Completed orders
* Cancellation rate

The dashboard should show actionable information before analytics.

---

# 10.7 Medicine Request Inbox

Columns:

* Request reference
* Medicine
* Strength
* Dosage form
* Quantity
* Patient area
* Fulfillment type
* Prescription status
* Urgency
* Received time
* Expiration
* Status

Filters:

* Branch
* Status
* Area
* Fulfillment type
* Prescription required
* Urgency
* Date

Actions:

* View request
* Submit offer
* Reject request

The pharmacy must not see unnecessary patient personal information before an order is selected.

---

# 10.8 Pharmacy Request Details

Display:

* Exact medicine package
* Quantity
* General service area
* Fulfillment preference
* Request expiration
* Prescription status
* Patient notes where authorized
* Existing pharmacy response

Prescription access:

* Available only to authorized pharmacists
* Requires logged access
* May require confirmation before opening
* Must not allow public URLs
* Must not expose the patient's complete profile

Release 1 request qualification is completed before matching by an authorized
designated licensed pharmacist using the administration/compliance review
interface. A branch pharmacist may perform an additional fulfillment check
after matching but cannot replace, edit, or bypass the qualification decision.

---

# 10.9 Create Offer

Fields:

* Branch
* Available quantity
* Unit price
* Preparation time
* Delivery available
* Delivery fee
* Offer validity
* Pharmacy notes

Validation:

* Quantity must be valid
* Price cannot be negative
* Offer expiry must be in the future
* Branch must be eligible
* User must have branch permission
* Request must remain open
* Prescription must have correct review state

The portal should calculate:

```text
Subtotal = quantity Ã— unit price
Total = subtotal + delivery fee
```

---

# 10.10 Offer Management

Offer list fields:

* Offer reference
* Request
* Medicine
* Branch
* Price
* Quantity
* Created time
* Expiration
* Status

Actions:

* View
* Withdraw
* Edit before selection, subject to policy
* View associated order

Editing an active offer should be audited.

---

# 10.11 Order Management

Order list columns:

* Order reference
* Medicine
* Quantity
* Patient area
* Fulfillment type
* Total
* Status
* Created time
* Required action

Actions by status:

### Pending confirmation

* Confirm
* Reject with reason

### Confirmed

* Start preparation
* Cancel with reason

### Preparing

* Mark ready for pickup
* Mark out for delivery
* Cancel with reason

### Ready for pickup

* Confirm collection

### Out for delivery

* Mark delivered
* Mark failed

Every status change should require confirmation and be audited.

---

# 10.12 Pharmacy Staff Management

Pharmacy owners can:

* Invite staff
* Assign branches
* Assign roles
* Disable staff
* Reset staff access
* View last login

Suggested permissions:

```text
VIEW_REQUESTS
VIEW_PRESCRIPTIONS
CREATE_OFFERS
MANAGE_ORDERS
MANAGE_BRANCH
MANAGE_STAFF
VIEW_REPORTS
MANAGE_DOCUMENTS
```

A pharmacist role is required to view or approve prescription-related information.

---

# 10.13 Branch Management

Functions:

* View branches
* Create branch
* Edit branch
* Set operating hours
* Set service areas
* Configure pickup
* Configure delivery
* Temporarily stop receiving requests
* Configure holiday closures

Changes to service area or operational status should take effect immediately or at a specified time.

---

# 10.14 Pharmacy Reports

MVP reports:

* Requests received
* Offers submitted
* Offers selected
* Orders completed
* Orders cancelled
* Response rate
* Average response time
* Fulfillment rate
* Orders by branch
* Orders by date
* Most requested medicines

Export formats:

* CSV
* Excel later

Sensitive patient information must not appear in exported operational reports unless required and authorized.

---

# 10.15 Pharmacy Support

Pharmacy users should be able to:

* Open support ticket
* Select branch
* Select order or request
* Add details
* Attach document
* Receive support replies
* Track resolution

---

# 11. Administration Web Portal

# 11.1 Administration Roles

Suggested roles:

```text
SYSTEM_ADMIN
COMPLIANCE_ADMIN
SUPPORT_AGENT
CATALOGUE_MANAGER
OPERATIONS_MANAGER
REPORT_VIEWER
```

Use permission-based access rather than relying only on broad role names.

---

# 11.2 Administration Navigation

Recommended navigation:

1. Dashboard
2. Pharmacy Applications
3. Pharmacies
4. Branches
5. Users
6. Medicine Catalogue
7. Prescriptions
8. Medicine Requests
9. Offers
10. Orders
11. Support
12. Safety Incidents
13. Notifications
14. Reports
15. Audit Logs
16. Cities and Areas
17. Configuration
18. Roles and Permissions

---

# 11.3 Administration Dashboard

Main cards:

* New pharmacy applications
* Active pharmacies
* Active branches
* Requests today
* Requests without offers
* Offers today
* Orders requiring attention
* Completed orders
* Cancelled orders
* Open complaints
* Safety incidents
* Notification failures

Charts:

* Requests over time
* Offer conversion
* Fulfillment rate
* Requests by city
* Requests by medicine category
* Pharmacy response performance

The dashboard must allow date-range filtering.

---

# 11.4 Pharmacy Application Review

Admin reviewers can:

* View submitted data
* View uploaded documents
* Check expiry dates
* Add internal notes
* Request corrections
* Approve application
* Reject application
* Suspend approved pharmacy

Approval should require:

* Confirmation
* Reviewer identity
* Review note
* Audit record

Rejection should require a reason visible to the pharmacy.

---

# 11.5 Pharmacy and Branch Management

Functions:

* Search pharmacies
* View status
* View branch list
* View documents
* View staff
* Suspend pharmacy
* Suspend branch
* Reactivate
* Change service areas
* View operational history
* View complaints
* View performance

High-risk actions should require confirmation and may require a second approval later.

---

# 11.6 User Management

Functions:

* Search patient or pharmacy user
* View account status
* View active sessions
* Suspend account
* Reactivate account
* Force logout
* Review support history
* Review consent status
* Process account-deletion request

Administrators should not see prescription content unless their role explicitly permits it.

---

# 11.7 Medicine Catalogue Management

Functions:

* Create medicine product
* Create package variation
* Edit Arabic and English names
* Manage active ingredients
* Manage dosage forms
* Manage strengths
* Manage manufacturers
* Add barcode
* Set prescription requirement
* Set restricted status
* Disable catalogue item
* Manage search aliases
* Import CSV
* Export catalogue
* Review import errors

Changes affecting medical eligibility or restrictions must be audited.

---

# 11.8 Prescription Review Interface

Authorized users can:

* View pending prescriptions
* Open secure file
* Approve
* Reject
* Request clarification
* Add review comment
* View review history

The portal should display:

* Patient request reference
* Requested medicine
* Quantity
* Prescription file
* Upload time
* Review status
* Previous review actions

Prescription access should be restricted mainly to pharmacy professionals or compliance users according to the approved operating model.

---

# 11.9 Request Monitoring

Admin request list:

* Request reference
* Patient ID
* Medicine
* Quantity
* City
* Area
* Status
* Prescription status
* Offers received
* Created time
* Expiration

Actions:

* View timeline
* View matching results
* Expand matching manually
* Cancel for valid operational reason
* Escalate
* Open support case

Manual changes must record reason and actor.

---

# 11.10 Offer Monitoring

Display:

* Offer reference
* Request
* Pharmacy
* Branch
* Quantity
* Price
* Delivery fee
* Status
* Creation time
* Expiry
* Selection status

Admins may:

* View suspicious pricing
* Review repeated withdrawals
* Review pharmacy response patterns
* Disable an invalid offer
* Escalate pharmacy behavior

---

# 11.11 Order Monitoring

Display:

* Order reference
* Patient
* Pharmacy
* Branch
* Medicine
* Quantity
* Price
* Fulfillment type
* Status
* Created time
* Last update

Actions:

* View complete timeline
* View cancellation reason
* Open support ticket
* Escalate incident
* Perform authorized manual correction

Manual order-status overrides should be rare and highly audited.

---

# 11.12 Support Management

Support agents need:

* Ticket queue
* Priority
* Category
* Assigned agent
* Related patient
* Related pharmacy
* Related request or order
* Conversation
* Attachments
* Internal notes
* SLA timers
* Escalation status

Support statuses:

```text
Open
In progress
Waiting for customer
Waiting for pharmacy
Escalated
Resolved
Closed
```

---

# 11.13 Safety Incident Management

Safety incidents must be separated from ordinary support.

Categories may include:

* Wrong medicine
* Wrong strength
* Damaged package
* Suspected counterfeit
* Prescription misuse
* Privacy breach
* Storage concern
* Delivery-temperature concern
* Adverse-event report

The portal should support:

* Incident severity
* Assigned compliance owner
* Investigation notes
* Related users
* Related pharmacy
* Related order
* Evidence
* Resolution
* External reporting reference where required

---

# 11.14 Audit Log Viewer

Filters:

* Actor
* Role
* Action
* Entity type
* Entity ID
* Date range
* IP address
* Correlation ID

Audit details:

* Timestamp
* Actor
* Action
* Affected entity
* Old values
* New values
* Request information

Audit logs should be read-only.

---

# 11.15 System Configuration

Configurable values:

* Supported cities
* Supported areas
* Request expiration
* Offer expiration
* Maximum quantity
* OTP expiration
* OTP attempt limit
* Pharmacy matching batch size
* Search radius
* Notification templates
* Support categories
* Cancellation reasons
* Restricted medicine categories
* File size limits
* Prescription retention period

Configuration changes must be versioned and audited.

---

# 12. Portal Responsive Design

The pharmacy portal should support:

* Desktop
* Tablet
* Mobile browser for simple operational actions

The admin portal should prioritize desktop and tablet.

Complex administration tables do not need full mobile optimization for MVP, but critical actions should remain usable on standard laptop screens.

---

# 13. Shared Design System

The applications should use a shared visual language.

## 13.1 Components

Required components:

* Buttons
* Inputs
* Search fields
* Select lists
* Date pickers
* Upload controls
* Cards
* Tables
* Status badges
* Tabs
* Confirmation dialogs
* Toast notifications
* Pagination
* Empty states
* Loading skeletons
* Error panels
* Timeline
* File viewer

## 13.2 Status consistency

The same status should use the same wording across all applications.

Example:

```text
OFFERS_RECEIVED
Arabic: ØªÙ… Ø§Ø³ØªÙ„Ø§Ù… Ø¹Ø±ÙˆØ¶
English: Offers received
```

Frontend applications should not generate business-status labels independently.

Role codes, permission codes, status codes, error codes, cancellation/rejection
reasons, and transition availability come from the versioned backend contract
or configuration API. The clients may map canonical codes to translated labels
and presentation tokens but must not invent additional business states.

---

# 14. Accessibility Requirements

The systems should provide:

* Sufficient contrast
* Scalable text
* Keyboard navigation in portals
* Visible focus state
* Screen-reader labels
* Accessible form errors
* Descriptive button text
* Alternative text for meaningful images
* No information represented only by color
* Logical Arabic right-to-left reading order

Release 1 targets WCAG 2.2 AA for the public website and web portals, with
documented exceptions requiring product and accessibility review. Automated
checks do not replace keyboard, screen-reader, zoom, contrast, and RTL manual
testing.

---

# 15. Client-Side Security

All applications must:

* Use HTTPS only
* Avoid logging tokens
* Avoid exposing stack traces
* Sanitize user-generated content
* Enforce secure session handling
* Handle token expiration
* Clear sensitive state on logout
* Validate uploaded files before sending
* Respect backend authorization responses
* Avoid storing prescription links
* Mask personal data where possible
* Use `HttpOnly`, `Secure`, and appropriate `SameSite` cookies for portal sessions
* Send and validate CSRF protection on cookie-authenticated mutations
* Apply a restrictive Content Security Policy to portal and public-web deployments
* Never store portal access or refresh tokens in `localStorage`

Frontend permission checks improve UX but do not replace backend authorization.

---

# 16. API Error Handling

The clients should map backend error codes to user-friendly messages.

Example codes:

```text
INVALID_OTP
OTP_EXPIRED
ACCOUNT_SUSPENDED
PRESCRIPTION_REQUIRED
PRESCRIPTION_REJECTED
REQUEST_EXPIRED
AVAILABILITY_ALERT_ALREADY_ACTIVE
OFFER_EXPIRED
OFFER_ALREADY_SELECTED
PHARMACY_NOT_APPROVED
ORDER_STATUS_INVALID
FILE_TOO_LARGE
UNSUPPORTED_FILE_TYPE
FILE_SCAN_PENDING
FILE_REJECTED
INCIDENT_REQUIRES_EMERGENCY_GUIDANCE
```

The client should:

* Display field errors beside inputs
* Display business errors as clear alerts
* Display retry option for network errors
* Include support reference for unexpected errors
* Log correlation IDs to error monitoring

---

# 17. Analytics Requirements

The patient application should track:

* Registration started
* Registration completed
* Medicine searched
* No search result
* Request started
* Request submitted
* Prescription uploaded
* Offer viewed
* Offer selected
* Order completed
* Support ticket created

The pharmacy portal should track:

* Request viewed
* Offer submitted
* Offer withdrawn
* Order confirmed
* Order cancelled
* Order completed

Do not send medicine names, prescription data, or personally identifiable medical information to third-party analytics tools.

Use internal identifiers or non-sensitive event categories.

---

# 18. Mobile Application Testing

Required tests:

* Component tests
* Form-validation tests
* Navigation tests
* Authentication tests
* API integration tests
* Upload tests
* Deep-link tests
* Notification tests
* RTL layout tests
* Arabic input tests
* Android device tests
* iOS device tests
* Slow-network tests
* Session-expiration tests
* Availability-alert tests
* Safety-incident and emergency-guidance tests

Critical mobile end-to-end flow:

```text
Register
â†’ search medicine
â†’ create request
â†’ upload prescription
â†’ receive offer
â†’ select offer
â†’ track order
â†’ complete order
```

---

# 19. Portal Testing

Required tests:

* Component tests
* Form tests
* Permission tests
* Branch-isolation tests
* Table filtering
* Pagination
* File access
* Pharmacy approval
* Offer submission
* Order status transitions
* Audit-log creation
* Arabic and English layouts
* Responsive behavior
* Generated-client/OpenAPI compatibility
* Cookie-session and CSRF behavior
* Concurrent offer-selection outcome through the complete client flow

The public website requires accessibility, RTL, content-publication,
policy-version, public-catalogue privacy, responsive, and search metadata tests.

Critical pharmacy flow:

```text
Login
â†’ view request
â†’ view prescription
â†’ submit offer
â†’ confirm selected order
â†’ prepare
â†’ complete pickup
```

Critical administration flow:

```text
Review pharmacy
â†’ approve branch
â†’ manage medicine catalogue
â†’ monitor request
â†’ review support case
â†’ inspect audit log
```

---

# 20. Application Deployment

## 20.1 Mobile deployment

Required accounts:

* Apple Developer account
* Google Play Console account
* Firebase project
* Expo account if Expo services are used
* Production application signing keys

Build environments:

* Development
* Preview
* Staging
* Production

The application should use different API endpoints and notification configurations for each environment.

## 20.2 Portal deployment

Deploy separately:

```text
www.dawak.example
pharmacy.dawak.example
admin.dawak.example
```

Requirements:

* HTTPS
* Environment-specific configuration
* Security headers
* Restricted admin access
* CDN for static assets
* Error monitoring
* Deployment rollback

The admin portal should not be publicly indexed by search engines.
The public website is indexable only for approved published pages. Pharmacy and
administration portals must not be indexed. Prefer standard Next.js
Node/Docker deployment behavior and avoid a custom server unless a documented
requirement cannot be met otherwise.

## 20.3 Cross-application delivery rule

Teams deliver vertical slices through the shared API contract rather than
finishing one application in isolation. The preferred sequence is:

1. Authentication and pharmacy approval walking skeleton
2. Admin catalogue to patient search
3. Prescription upload/review and request creation
4. Matching, pharmacy inbox/offers, and patient comparison
5. Transaction-safe selection and pickup completion
6. Basic pharmacy-managed delivery
7. Notifications, availability alerts, support, incidents, and public content
8. Hardening and pilot acceptance

Pickup is the first complete fulfillment path. Basic delivery follows after
the pickup slice is stable; live tracking remains Release 2.

---

# 21. Suggested Mobile Application Development Phases

## Phase 1 â€” Foundation

* Project setup
* Navigation
* Design system
* Arabic and English
* API client
* Secure storage
* Authentication flow
* Error handling

## Phase 2 â€” Medicine catalogue

* Home
* Medicine search
* Search results
* Medicine details
* Recent searches

## Phase 3 â€” Request flow

* Create request
* Prescription upload
* Request review
* Request submission
* Request history
* Request details

## Phase 4 â€” Offers and orders

* Offers list
* Offer details
* Offer selection
* Order details
* Order tracking
* Cancellation

## Phase 5 â€” Notifications and support

* Device-token registration
* Notification inbox
* Deep links
* Support tickets
* Profile and settings

## Phase 6 â€” Hardening

* Accessibility
* Performance
* Error monitoring
* Security testing
* Store preparation
* Pilot release

---

# 22. Suggested Pharmacy Portal Development Phases

## Phase 1 â€” Authentication and layout

* Login
* Password recovery
* Main layout
* Permissions
* Localization

## Phase 2 â€” Pharmacy onboarding

* Pharmacy application
* Branch management
* Document upload
* Approval status
* Staff setup

## Phase 3 â€” Requests and prescriptions

* Request inbox
* Filters
* Request details
* Secure prescription view
* Reject request

## Phase 4 â€” Offers

* Create offer
* Offer list
* Offer details
* Offer expiration
* Offer withdrawal

## Phase 5 â€” Orders

* Order queue
* Status transitions
* Pickup flow
* Delivery flow
* Cancellation

## Phase 6 â€” Reporting and settings

* Dashboard
* Reports
* Operating hours
* Service areas
* Staff permissions
* Support

---

# 23. Suggested Administration Portal Development Phases

## Phase 1 â€” Authentication and permissions

* Admin login
* MFA
* Roles and permissions
* Secure layout
* Audit foundation

## Phase 2 â€” Pharmacy approval

* Applications
* Documents
* Review actions
* Approval and rejection
* Suspension

## Phase 3 â€” Catalogue administration

* Medicine products
* Packages
* Active ingredients
* Dosage forms
* Manufacturers
* Import and export

## Phase 4 â€” Marketplace operations

* Requests
* Offers
* Orders
* Manual escalations
* Monitoring

## Phase 5 â€” Support and compliance

* Tickets
* Safety incidents
* Prescription review
* User management
* Audit logs

## Phase 6 â€” Reporting and configuration

* Dashboard
* Operational reports
* Locations
* Business settings
* Notification templates

---

# 24. Release Priority Matrix

## Release 1 must have

### Mobile

* OTP login
* Medicine search
* Request creation
* Prescription upload
* Offer list
* Offer selection
* Order tracking
* Notifications
* Availability alerts and no-offer follow-up
* Support
* Receipt confirmation and safety-incident reporting

### Public website

* How Dawak works
* Supported cities and areas
* Public catalogue information
* FAQs, articles, policies, and safety notices
* Arabic-first responsive and accessible experience

### Pharmacy portal

* Authentication
* Onboarding
* Branch management
* Request inbox
* Prescription view
* Offer submission
* Order management
* Staff permissions

### Admin portal

* Authentication and MFA
* Pharmacy approval
* Catalogue management
* Request monitoring
* Order monitoring
* Support
* Audit logs
* Configuration
* Content publication
* Safety-incident management
* Basic operational dashboard and reports

## Release 1 should have

* Pharmacy reporting
* Delivery fee configuration
* Service-area map
* CSV export

## Release 2

* In-app chat
* Family profiles
* Online payments
* Live delivery map
* Pharmacy subscription plans
* Saved medicines
* Refill reminders
* Pharmacy Excel stock import
* Charity assistance workflow
* Enhanced fraud controls
* City expansion and pharmacy-chain operations

## Excluded from Releases 1 and 2

* Customer medicine listings
* Customer-to-customer sales
* Customer-to-customer donations
* Automatic medical substitutions
* AI diagnosis
* Doctor consultation
* Insurance claims
* Full pharmacy POS integration
* Controlled-medicine workflows
* Ratings unless separately approved for privacy and medical-order safety
* Approved substitution support
* Digital prescriptions and electronic traceability

---

# 25. Team Requirements

Recommended delivery team:

| Role                   | Responsibility                           |
| ---------------------- | ---------------------------------------- |
| Product manager        | Scope, priorities, workflows             |
| UI/UX designer         | Mobile and portal design                 |
| React Native developer | Patient application                      |
| Frontend developer     | Pharmacy and admin portals               |
| Spring Boot developer  | Backend APIs                             |
| QA engineer            | Mobile, portal, and API testing          |
| DevOps engineer        | Deployment and monitoring                |
| Pharmacist advisor     | Prescription and pharmacy workflows      |
| Security reviewer      | Authentication and medical-data security |

A small team may combine roles, but healthcare and pharmacy workflow review should remain independent from software development.

---

# 26. Required Design Deliverables

Before frontend development, prepare:

* User-flow diagrams
* Mobile wireframes
* Public website wireframes
* Pharmacy portal wireframes
* Admin portal wireframes
* Design system
* Arabic and English content
* Status glossary
* Form-validation rules
* Responsive layouts
* Clickable prototype
* Accessibility review
* API contract
* Threat model and data-retention matrix
* Canonical role/permission, status, error, and reason glossary

Essential prototype journeys:

1. Patient submits a request
2. Pharmacy submits an offer
3. Patient selects an offer
4. Pharmacy completes an order
5. Administrator approves a pharmacy
6. Support agent resolves a complaint
7. Patient enables/cancels an availability alert
8. Patient reports a safety incident
9. Visitor reads supported areas and current policies

---

# 27. Definition of Done

## Public website

The public website is complete when:

* Current supported areas, FAQs, how-it-works, policies, and safety notices are available
* Public catalogue information does not expose private availability or patient data
* Arabic RTL and English layouts meet the accessibility target
* Only published content is visible and policy versions are correct
* Private portal sessions are not shared with the public application

## Patient mobile application

The mobile MVP is complete when:

* Registration and OTP work securely
* Arabic and English layouts work
* Medicine search returns structured packages
* A patient can create a valid request
* Prescription upload is secure
* Offers are displayed correctly
* Only one active offer can be selected
* Orders update correctly
* Push notifications open the correct screen
* Availability alerts can be enabled, paused, and cancelled without bypassing eligibility rules
* Receipt confirmation, support, and safety-incident reporting work
* Sensitive information is not stored insecurely
* Critical flows pass Android and iOS testing

## Pharmacy portal

The portal is complete when:

* Pharmacy users can authenticate
* Onboarding and approval statuses work
* Branch access is isolated
* Authorized branch pharmacists can perform the permitted fulfillment check
  without changing or bypassing the designated qualification decision
* Offers can be created and withdrawn
* Selected orders can be fulfilled
* Status changes are validated
* Staff permissions are enforced
* Reports display reliable data

## Administration portal

The portal is complete when:

* Admin MFA is active
* Pharmacy review works
* Catalogue records can be managed
* A designated authorized pharmacist can qualify, reject, or request clarification for a prescription before matching
* Requests, offers, and orders can be monitored
* Support and incidents can be managed
* Sensitive actions are audited
* Permissions prevent unauthorized access
* Configuration changes are versioned

---

# 28. Final Recommendation

Build the Dawak Release 1 client surface as four focused products:

## Patient mobile application

Optimized for simplicity, Arabic usability, medicine search, request submission, offer comparison, and order tracking.

## Public website

Optimized for public trust, supported-area discovery, safe catalogue
information, policies, content, accessibility, and mobile-app acquisition.

## Pharmacy web portal

Optimized for fast operational decisions, including reviewing requests, submitting offers, and fulfilling orders.

## Administration web portal

Optimized for trust, compliance, catalogue control, operational monitoring, support, and auditability.

The first release should focus on completing one reliable journey across the
mobile application, pharmacy portal, administration portal, and backend, while
the public website explains and supports that journey:

> The patient submits an exact medicine request, a verified pharmacy responds, the patient selects the offer, the pharmacy fulfills the order, and the administrator can monitor the complete process.

## 28.1 Release 2 client extension

Release 2 adds dedicated flows for online payment, pharmacy subscriptions, live
delivery tracking, saved medicines/refill reminders, family/caregiver access,
charity assistance, pharmacy Excel stock import, enhanced fraud review, masked
chat, and city/chain expansion. These require a separate design/API/security
refinement and must not be exposed by Release 1 feature flags or navigation.
Beta
0 / 0
used queries
1