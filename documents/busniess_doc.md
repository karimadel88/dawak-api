# دواء — MVP Business Document

## 1. Executive Summary

**دواء** is a digital medicine-access platform that helps patients find medicines, request hard-to-find products, upload prescriptions where required, and receive offers from licensed pharmacies.

The platform may also support medicine-donation initiatives through approved charitable organizations, hospitals, pharmacies, and regulated collection programs. Individuals do not sell medicines directly to other individuals.

دواء addresses four major customer problems:

1. Difficulty finding medicines across multiple pharmacies.
2. Time wasted calling or visiting pharmacies.
3. Lack of reliable information about availability.
4. Limited coordination between patients, pharmacies, charities, and medicine-support initiatives.

The MVP will initially operate as a **medicine search and request platform**, not an independent pharmacy and not a customer-to-customer medicine marketplace.

---

# 2. Recommended Brand Name

## Primary recommendation: دواء

**Arabic:** دواء
**English:** Dawaa
**Suggested domain style:** Dawaa App, GetDawaa, Dawaa Egypt

### Advantages

* Simple and immediately understandable.
* Strong connection to the product category.
* Easy to pronounce in Arabic and English.
* Suitable for patients, pharmacies, charities, and healthcare providers.
* Flexible enough to support future services such as reminders, delivery, consultations, and health products.

### Potential limitation

The word is generic, which may make trademarks, domain names, application-store naming, and search-engine differentiation more difficult.

## Alternative names

| Name              | Meaning and positioning                                                            |
| ----------------- | ---------------------------------------------------------------------------------- |
| **دواك**          | “Your medicine”; personal and easy to remember.                                    |
| **لقيت دوا**      | Strong connection to medicine discovery.                                           |
| **دوائي**         | Personal health and medication-management positioning.                             |
| **صيدلي**         | Builds trust around pharmacist involvement.                                        |
| **متوفر**         | Focuses directly on medicine availability.                                         |
| **شفا**           | Emotional, healthcare-oriented brand with expansion potential.                     |
| **دوا موجود**     | Clear promise that the platform helps locate medicine.                             |
| **دواء بلس**      | Allows expansion into additional healthcare services.                              |
| **MedFind Egypt** | Modern bilingual medicine-discovery positioning.                                   |
| **Dawini**        | Friendly brand meaning “help treat me,” but requires careful healthcare messaging. |

## Final naming recommendation

Use:

# دواك

**Tagline:**
دواك أقرب مما تتخيل

**English tagline:**
Find your medicine faster.

“دواك” is more distinctive and personal than “دواء,” while still communicating the platform’s purpose immediately.

---

# 3. Business Vision

To become Egypt’s trusted digital platform for locating medicines and coordinating safe access through licensed pharmacies and approved healthcare partners.

---

# 4. Mission

To reduce the time, cost, and uncertainty involved in finding medicines while maintaining patient safety, prescription controls, privacy, and regulated pharmaceutical distribution.

---

# 5. Problem Statement

Patients frequently experience difficulties such as:

* Visiting several pharmacies before finding a medicine.
* Calling many pharmacies individually.
* Receiving outdated availability information.
* Finding medicines with different concentrations or package sizes.
* Locating medicines during shortages.
* Managing recurring medication needs.
* Finding approved support when they cannot afford treatment.
* Understanding whether a prescription is required.
* Coordinating delivery from a trusted pharmacy.

Pharmacies also face operational problems:

* Receiving repetitive availability calls.
* Holding stock that customers cannot discover.
* Losing customers because their inventory is not searchable.
* Managing medicine requests through WhatsApp and phone calls.
* Difficulty measuring demand for unavailable products.
* Limited digital presence outside large pharmacy chains.

دواء creates a structured connection between medicine demand and licensed medicine supply.

---

# 6. Proposed Business Model

## 6.1 Core model

دواء operates as a digital intermediary between:

* Patients.
* Licensed pharmacies.
* Pharmacy chains.
* Approved delivery providers.
* Charitable organizations.
* Hospitals and healthcare initiatives.
* Platform administrators.

A patient submits a medicine request. Nearby or selected pharmacies review the request and respond with availability, price, preparation time, and delivery or pickup options.

The patient selects an offer and completes the order through the pharmacy.

## 6.2 What the platform will not do in the MVP

The platform will not:

* Allow individuals to sell medicines.
* Allow individuals to exchange opened medicine packages.
* Permit anonymous medicine listings.
* Repackage medicines.
* Recommend prescription medicines without professional involvement.
* Replace doctors or pharmacists.
* Guarantee medicine suitability for a specific patient.
* Accept expired, damaged, opened, incorrectly stored, or recalled medicines.
* Operate as an unlicensed medicine warehouse.
* Set or manipulate regulated medicine prices.
* Allow controlled medicines in unsupported workflows.

---

# 7. Target Customers

## 7.1 Primary customer segments

### Patients and families

People searching for prescription and non-prescription medicines.

### Chronic-disease patients

Patients who repeatedly need medicines for diabetes, blood pressure, heart disease, thyroid conditions, asthma, or other long-term needs.

### Caregivers

People managing medicines for children, elderly family members, or patients with disabilities.

### Licensed pharmacies

Independent pharmacies and pharmacy chains seeking additional customer demand and digital visibility.

## 7.2 Secondary customer segments

* Charitable healthcare organizations.
* Hospitals and clinics.
* Pharmaceutical manufacturers.
* Insurance providers.
* Delivery companies.
* Employee healthcare programs.
* Patient-support programs.

---

# 8. User Roles

| Role                     | Responsibilities                                                                                                  |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------- |
| Visitor                  | Searches public medicine information, views supported areas, reads FAQs, and learns how the service works.        |
| Patient                  | Creates requests, uploads prescriptions, compares pharmacy offers, chooses pickup or delivery, and tracks orders. |
| Caregiver                | Manages requests for a family member with the appropriate consent.                                                |
| Pharmacy user            | Manages branch information, availability requests, offers, order preparation, and fulfillment status.             |
| Pharmacist               | Reviews prescriptions and validates medicine-related requests where required.                                     |
| Charity representative   | Reviews eligible assistance cases and coordinates approved medicine-support requests.                             |
| Delivery partner         | Receives assigned deliveries without unnecessary access to medical information.                                   |
| Administrator            | Approves partners, manages catalogue data, reviews incidents, monitors performance, and handles support.          |
| Compliance administrator | Reviews pharmacy documents, suspicious activity, prescription workflows, recalls, and audit logs.                 |

---

# 9. MVP Value Proposition

## For patients

“Submit one request and receive availability responses from trusted pharmacies instead of searching pharmacy by pharmacy.”

## For pharmacies

“Receive qualified medicine requests, attract nearby customers, and manage demand through one simple dashboard.”

## For charities

“Receive structured, verified medicine-support cases rather than unorganized social-media requests.”

---

# 10. MVP Features

## 10.1 Patient application

### Account registration

* Mobile-number registration.
* OTP verification.
* Name and basic profile information.
* Optional email address.
* Preferred city and area.
* Consent to privacy policy and terms.

### Medicine search

Patients can search using:

* Brand name.
* Active ingredient.
* Arabic or English name.
* Concentration.
* dosage form.
* Package size.
* Manufacturer.
* Barcode, when supported.

### Medicine request

A request contains:

* Medicine.
* Required quantity.
* Preferred area.
* Pickup or delivery preference.
* Urgency.
* Prescription attachment when required.
* Optional patient notes.

### Pharmacy offers

The patient sees:

* Pharmacy name.
* Branch.
* Verified status.
* Available quantity.
* Approved price.
* Estimated preparation time.
* Pickup or delivery availability.
* Delivery fee.
* Offer expiration time.

### Order tracking

Suggested statuses:

* Request submitted.
* Under prescription review.
* Sent to pharmacies.
* Offers received.
* Offer selected.
* Pharmacy preparing.
* Ready for pickup.
* Out for delivery.
* Delivered.
* Cancelled.
* Unable to fulfill.

### Notifications

* Request received.
* Prescription accepted or requires correction.
* New pharmacy offer.
* Offer expiring.
* Order ready.
* Delivery update.
* Refill reminder.

---

## 10.2 Pharmacy portal

### Pharmacy onboarding

The pharmacy provides:

* Legal pharmacy name.
* License information.
* Tax and commercial information where required.
* Branch address.
* Responsible pharmacist details.
* Contact information.
* Operating hours.
* Delivery coverage.
* Settlement information.
* Supporting documents.

The pharmacy remains inactive until administrator verification.

### Request inbox

Pharmacies receive relevant requests based on:

* Geographic area.
* Branch service radius.
* Medicine category.
* Opening hours.
* Delivery capability.
* Prescription-handling capability.

### Create offer

The pharmacy can provide:

* Available quantity.
* Price.
* Preparation time.
* Offer expiration.
* Pickup details.
* Delivery fee.
* Substitute availability, subject to pharmacist review.

### Order management

* Accept selected order.
* Begin preparation.
* Mark ready.
* Assign delivery.
* Confirm collection.
* Mark delivered.
* Record fulfillment failure.
* Manage cancellation reason.

### Stock management

For the first MVP release, pharmacies may update availability manually.

Later options:

* Excel upload.
* POS integration.
* Pharmacy ERP integration.
* Inventory API.
* Barcode-based stock updates.

---

## 10.3 Administration portal

### Partner management

* Review pharmacy applications.
* Approve, suspend, or reject pharmacies.
* Verify expiring licenses and documents.
* Manage pharmacy branches.
* Review pharmacists and staff accounts.

### Medicine catalogue

* Manage medicines.
* Manage active ingredients.
* Manage dosage forms.
* Manage strengths.
* Mark prescription requirements.
* Mark restricted products.
* Record storage requirements.
* Disable recalled or unavailable products.

### Request monitoring

* View active requests.
* Identify requests without offers.
* Review cancellations.
* Review prescription exceptions.
* Detect repeated or suspicious requests.

### Customer support

* Support tickets.
* Complaints.
* Pharmacy disputes.
* Delivery complaints.
* Privacy requests.
* Refund-related coordination.
* Incident escalation.

### Content management

* FAQs.
* Health-awareness articles.
* Platform notices.
* Supported cities.
* Terms and privacy documents.
* Safety announcements.

---

# 11. Core MVP Workflow

## 11.1 Standard medicine request

1. Patient searches for a medicine.
2. Patient selects the exact concentration, form, and package.
3. Patient enters the required quantity.
4. The system determines whether a prescription is required.
5. The patient uploads the prescription when necessary.
6. The request is validated.
7. Matching licensed pharmacies receive the request.
8. Pharmacies submit availability offers.
9. The patient compares offers.
10. The patient selects one offer.
11. The pharmacy confirms the order.
12. The patient pays through the approved method.
13. The order is prepared.
14. The order is collected or delivered.
15. The patient confirms receipt or reports an issue.

## 11.2 Prescription request

1. Patient uploads a clear prescription.
2. The prescription is reviewed by the participating pharmacy or designated pharmacist.
3. The requested medicine, quantity, and validity are checked.
4. The request is accepted, rejected, or returned for clarification.
5. Only eligible pharmacies receive or fulfill the request.
6. Prescription access is logged for auditing.

## 11.3 No-availability workflow

When no pharmacy responds:

1. The patient may expand the search radius.
2. The patient may enable an availability alert.
3. The request may be escalated to additional pharmacy partners.
4. A pharmacist-approved substitute workflow may be offered.
5. Eligible affordability cases may be referred to approved assistance partners.

---

# 12. Donation and Assistance Model

## 12.1 Recommended MVP approach

Do not launch direct medicine donations between customers.

Instead, introduce a **medicine-assistance request**.

A patient who cannot afford a medicine may submit:

* Required medicine.
* Prescription.
* Basic eligibility information.
* Supporting documents where necessary.
* Consent to share the case with selected assistance partners.

Approved charities or patient-support programs can then:

* Accept the case.
* Purchase the medicine from a licensed pharmacy.
* Arrange pickup or delivery.
* Record fulfillment.

## 12.2 Future medicine-return program

A future version may support collection of unused medicines only through approved partners and after obtaining appropriate legal and regulatory guidance.

Potential collection-point rules would include:

* Unopened packages only.
* Original packaging.
* Readable batch number.
* Readable expiry date.
* Sufficient remaining shelf life.
* No signs of damage.
* No cold-chain medicines unless a compliant process exists.
* No controlled medicines.
* No recalled products.
* No medicine accepted directly by another patient.
* Final inspection by an authorized professional.
* Reuse only when legally permitted.
* Otherwise, safe disposal through an approved process.

The Egyptian Drug Authority currently emphasizes controlled withdrawal and safe disposal of expired or unusable pharmaceutical products, so this feature should not be implemented without formal regulatory and legal approval.

---

# 13. Product Eligibility

## 13.1 Suitable initial categories

Subject to professional legal review, the MVP may begin with:

* Common non-prescription medicines.
* Prescription medicines fulfilled by licensed pharmacies.
* Mother-and-baby products.
* First-aid products.
* Approved medical supplies.
* Personal-care products.
* Selected medical devices.
* Diabetes-monitoring supplies.
* Vitamins sold through participating pharmacies, where permitted.

## 13.2 Restricted or excluded categories

Initially exclude:

* Narcotics.
* Psychotropic medicines.
* Controlled medicines.
* Opened packages.
* Partially used medicine strips.
* Medicines without original packaging.
* Expired products.
* Near-expiry products below the approved threshold.
* Recalled products.
* Samples marked “not for sale.”
* Unregistered products.
* Products without identifiable batch information.
* Products requiring unsupported cold-chain handling.
* Compounded medicines.
* Hospital-only products.
* High-risk oncology products.
* Injectable products unless explicitly supported later.
* Medicines offered by individual users.

---

# 14. Trust and Safety Controls

## Partner verification

Every pharmacy should be verified before activation.

## Prescription protection

Prescription files should be:

* Encrypted during transmission.
* Encrypted in storage.
* Visible only to authorized users.
* Retained only for a defined period.
* Recorded in an access log.
* Deleted or anonymized according to policy.

## Product matching

The system must distinguish between:

* Brand.
* Active ingredient.
* Concentration.
* Dosage form.
* Package size.
* Route of administration.

The interface must prevent accidental substitution based only on a similar name.

## Quantity controls

The system may enforce:

* Maximum request quantities.
* Repeat-request monitoring.
* Prescription quantity validation.
* Restrictions for selected products.
* Manual pharmacist review.

## Audit trail

Record:

* Who viewed a prescription.
* Who approved a pharmacy.
* Who changed an order status.
* Who changed medicine restrictions.
* Who approved a substitute.
* When an offer was created or modified.
* Cancellation and rejection reasons.

## Incident reporting

Allow reporting of:

* Wrong product.
* Wrong concentration.
* Damaged package.
* Suspected counterfeit.
* Unexpected price.
* Delivery-temperature concern.
* Missing product.
* Privacy concern.
* Adverse reaction.

Medical emergencies and adverse reactions must be directed to appropriate medical and regulatory reporting channels rather than treated as ordinary customer-support tickets.

---

# 15. Business Rules

1. Only verified pharmacies can create medicine offers.
2. Patients cannot list medicines for sale.
3. Every offer belongs to a specific pharmacy branch.
4. An offer expires after a defined period.
5. A pharmacy cannot confirm more quantity than it has available.
6. The patient must select the exact medicine configuration.
7. Prescription-required products cannot proceed without the necessary validation.
8. A pharmacy can reject a request with a standardized reason.
9. Prices should be recorded exactly as supplied by the licensed pharmacy and handled in accordance with applicable pricing rules.
10. The platform must not present automatic substitution as medical advice.
11. Any substitute must be reviewed through a pharmacist-controlled workflow.
12. Delivery partners receive only the information needed to complete delivery.
13. A selected offer reserves stock for a limited period.
14. The reservation is released if payment or confirmation is not completed.
15. Suspicious accounts may be suspended pending review.
16. Recalled products must be immediately disabled across the platform.
17. Medicine catalogue changes require administrative authorization.
18. Completed medical orders should not expose sensitive details in public reviews.
19. Users must not upload prescriptions belonging to another person without authorization.
20. Every cancellation must have a recorded reason.

---

# 16. Functional Scope

## MVP Release 1

### Patient side

* Registration and login.
* Arabic-first interface.
* Medicine catalogue search.
* Medicine request creation.
* Prescription upload.
* Pharmacy offers.
* Offer selection.
* Pickup and delivery options.
* Order tracking.
* Push and SMS notifications.
* Customer support.

### Pharmacy side

* Pharmacy onboarding.
* Branch management.
* Request inbox.
* Offer creation.
* Order confirmation.
* Status updates.
* Basic performance dashboard.
* Staff accounts.

### Administration side

* Pharmacy verification.
* User management.
* Medicine catalogue.
* Request monitoring.
* Support and complaints.
* Restricted-product controls.
* Audit logs.
* Basic reports.
* Content management.

## MVP Release 2

* Online payments.
* Pharmacy subscription plans.
* Live delivery tracking.
* Refill reminders.
* Saved medicines.
* Family profiles.
* Charity-assistance workflow.
* Pharmacy Excel stock import.
* Better fraud controls.
* In-app chat with masked contact information.

## Future releases

* Pharmacy POS integrations.
* Insurance integration.
* Manufacturer patient-support programs.
* Digital prescription integration.
* Electronic medicine traceability.
* Pharmacist consultation.
* Corporate healthcare programs.
* Advanced demand analytics.
* Medicine-shortage heatmaps.
* AI-assisted catalogue matching with human validation.
* Approved substitution support.
* Home-care and laboratory integrations.

---

# 17. Revenue Model

## 17.1 Pharmacy subscription

Monthly subscription for pharmacy branches.

Possible plans:

| Plan       | Features                                                    |
| ---------- | ----------------------------------------------------------- |
| Starter    | Request inbox, manual offers, basic analytics.              |
| Growth     | Multiple staff accounts, delivery zones, advanced reports.  |
| Enterprise | Chain management, integrations, SLA, centralized reporting. |

## 17.2 Service fee

A platform fee may apply to successfully fulfilled orders where legally and commercially appropriate.

Possible models:

* Fixed fee per completed order.
* Percentage service fee on non-regulated products.
* Pharmacy-paid acquisition fee.
* Patient-paid delivery or convenience fee.

Medicine-pricing and platform-fee structures require specialist legal review before implementation.

## 17.3 Delivery revenue

The platform may receive a technology or coordination fee from delivery partners.

## 17.4 Sponsored pharmacy placement

Verified pharmacies may pay for clearly labelled placement, provided rankings remain transparent and do not compromise patient safety.

## 17.5 Business partnerships

Revenue may come from:

* Pharmacy chains.
* Insurance providers.
* Employers.
* Charities.
* Pharmaceutical patient-support programs.
* Healthcare institutions.

## 17.6 Analytics

Aggregated and anonymized demand insights may eventually be offered to qualified organizations, subject to privacy, legal, ethical, and contractual controls.

Individual prescription or patient data must not be sold.

---

# 18. Go-to-Market Strategy

## Phase 1: Controlled pilot

Launch in one limited geographic area.

Recommended starting areas:

* Cairo.
* Giza.
* One or two densely populated districts.
* A controlled network of 20–50 pharmacy branches.

### Pilot objectives

* Confirm patient demand.
* Measure pharmacy response times.
* Validate prescription workflows.
* Identify catalogue-matching errors.
* Understand delivery economics.
* Test order-conversion rates.

## Phase 2: City expansion

* Add pharmacy chains.
* Expand district coverage.
* Introduce payment options.
* Add refill reminders.
* Launch pharmacy subscriptions.

## Phase 3: National platform

* Expand to additional governorates.
* Add insurance and corporate programs.
* Integrate pharmacy inventory.
* Add charity and manufacturer-support workflows.

---

# 19. Customer Acquisition

## Patient acquisition channels

* Search-engine marketing for medicine availability.
* Social-media educational content.
* Pharmacy QR codes.
* Clinic and doctor referrals where appropriate.
* Chronic-disease communities.
* Patient-support organizations.
* Referral program.
* WhatsApp request entry point.
* App-store optimization.

## Pharmacy acquisition channels

* Direct pharmacy-sales team.
* Partnerships with pharmacy associations.
* Free pilot period.
* Branch onboarding support.
* Performance reports demonstrating new customer demand.
* Integration partnerships with pharmacy software providers.

---

# 20. Key Performance Indicators

## Marketplace KPIs

* Number of medicine requests.
* Percentage of requests receiving an offer.
* Average time to first offer.
* Average number of offers per request.
* Offer-selection rate.
* Fulfillment rate.
* Cancellation rate.
* Average order preparation time.
* Delivery success rate.

## Patient KPIs

* Registered patients.
* Active patients.
* Repeat request rate.
* Search-to-request conversion.
* Request-to-order conversion.
* Patient satisfaction.
* Complaint rate.
* Refill-reminder conversion.

## Pharmacy KPIs

* Active pharmacy branches.
* Pharmacy response rate.
* Average pharmacy response time.
* Offer acceptance rate.
* Fulfilled orders per branch.
* Pharmacy retention.
* Subscription conversion.

## Safety KPIs

* Prescription rejection rate.
* Wrong-product incidents.
* Wrong-concentration incidents.
* Suspected counterfeit reports.
* Privacy incidents.
* Restricted-product attempts.
* Complaints per 1,000 completed orders.
* Time required to resolve safety incidents.

## Business KPIs

* Gross merchandise value.
* Platform revenue.
* Revenue per active pharmacy.
* Cost per acquired patient.
* Cost per acquired pharmacy.
* Contribution margin per completed order.
* Monthly recurring revenue.
* Customer and pharmacy churn.

---

# 21. Non-Functional Requirements

## Security

* Encryption in transit and at rest.
* Role-based access control.
* Multi-factor authentication for administrators.
* Secure password storage.
* Rate limiting.
* Device and session management.
* Malware scanning for uploaded files.
* Security monitoring.
* Regular penetration testing.

## Privacy

* Data-minimization principles.
* Explicit consent.
* Purpose-based prescription access.
* Configurable retention periods.
* Account-deletion process.
* Data-export process.
* Privacy audit logs.
* Masked patient contact information.

## Reliability

* Automated backups.
* Disaster-recovery plan.
* Monitoring and alerting.
* Pharmacy-offer idempotency.
* Reliable notification retry.
* Order-state consistency.
* High availability for critical workflows.

## Performance

Initial targets:

* Search response under two seconds for normal usage.
* Request submission under three seconds excluding file upload.
* Pharmacy notification shortly after request qualification.
* Support for future horizontal scaling.

---

# 22. Suggested Technology Architecture

## Applications

* Patient mobile application.
* Responsive patient website.
* Pharmacy web portal.
* Administration portal.
* Backend API.
* Notification service.
* Search service.
* File-storage service.
* Reporting service.

## Suggested backend modules

* Identity and access.
* Patient profiles.
* Pharmacy onboarding.
* Pharmacy branches.
* Medicine catalogue.
* Prescription management.
* Requests.
* Offers.
* Orders.
* Payments.
* Delivery.
* Notifications.
* Support.
* Compliance.
* Reporting.
* Audit logs.

## Important design decision

Use separate entities for:

* Medicine catalogue item.
* Patient request.
* Pharmacy offer.
* Order.
* Prescription.
* Pharmacy branch.
* Delivery.

Do not use a single “advertisement” entity for the complete process. The new platform is request-and-fulfillment based rather than classified-ad based.

---

# 23. Main Data Entities

* User.
* Patient profile.
* Family member.
* Pharmacy.
* Pharmacy branch.
* Pharmacy license.
* Pharmacy employee.
* Pharmacist.
* Medicine.
* Active ingredient.
* Medicine package.
* Prescription.
* Medicine request.
* Request item.
* Pharmacy offer.
* Offer item.
* Stock reservation.
* Order.
* Order item.
* Payment.
* Delivery.
* Charity case.
* Notification.
* Support ticket.
* Complaint.
* Incident.
* Consent record.
* Audit event.

---

# 24. Major Risks and Mitigations

| Risk                                       | Mitigation                                                                                         |
| ------------------------------------------ | -------------------------------------------------------------------------------------------------- |
| Unlicensed medicine trading                | Permit offers only from verified licensed pharmacies.                                              |
| Counterfeit products                       | Pharmacy verification, batch information, incident reporting, and future traceability integration. |
| Incorrect medicine matching                | Structured catalogue with strength, form, package, ingredient, and barcode data.                   |
| Prescription misuse                        | Pharmacist validation, quantity controls, access logs, and suspicious-activity monitoring.         |
| Medicine stored incorrectly                | Do not accept inventory from individuals; establish pharmacy and delivery requirements.            |
| Privacy breach                             | Encryption, limited access, retention controls, and security monitoring.                           |
| Outdated availability                      | Offer expiration and short stock-reservation periods.                                              |
| Pharmacy fails to fulfill                  | Performance scoring, penalties, suspension rules, and patient support.                             |
| Regulatory change                          | Dedicated compliance owner and periodic legal review.                                              |
| Platform considered an unlicensed pharmacy | Clearly define the intermediary model and obtain legal advice before launch.                       |
| Unsafe substitutions                       | Pharmacist-controlled process only.                                                                |
| Delivery damage                            | Packaging standards, chain-of-custody records, and restricted cold-chain scope.                    |

---

# 25. Competitive Differentiation

دواء should not compete only as another online pharmacy.

Its differentiation is:

1. One request reaches multiple licensed pharmacies.
2. Strong focus on hard-to-find medicines.
3. Arabic-first medicine search.
4. Structured prescription handling.
5. Independent-pharmacy participation.
6. Availability alerts.
7. Patient-assistance coordination.
8. Demand analytics for pharmacies.
9. Future inventory and traceability integration.
10. Safety-first design rather than classified advertisements.

---

# 26. Changes from the Original Bziada Model

| Bziada model                       | Recommended دواء model                                     |
| ---------------------------------- | ---------------------------------------------------------- |
| Customer-to-customer marketplace   | Patient-to-licensed-pharmacy request platform              |
| Users create medicine ads          | Patients create medicine requests                          |
| Individual owners approve requests | Pharmacies submit and fulfill offers                       |
| Sale or donation listing           | Pharmacy purchase or approved assistance case              |
| User-provided medicine inventory   | Licensed-pharmacy inventory                                |
| Listing quantity deduction         | Pharmacy reservation and order fulfillment                 |
| Individual delivery contacts       | Pharmacy pickup or approved delivery                       |
| Supporting images from sellers     | Prescription and pharmacy-controlled product evidence      |
| Listing approval                   | Pharmacy verification and request validation               |
| Peer-to-peer donation              | Charity purchases medicine through licensed channels       |
| Marketplace value reporting        | Fulfillment, subscription, service, and delivery reporting |

---

# 27. MVP Acceptance Criteria

The MVP is ready for a controlled pilot when:

* Patients can register securely.
* A patient can find the exact medicine package.
* Prescription requirements are correctly identified.
* A prescription can be uploaded securely.
* A request can reach eligible pharmacies.
* A pharmacy can respond with an offer.
* The patient can select an offer.
* The pharmacy can confirm and prepare the order.
* Pickup or delivery can be completed.
* All important actions are audited.
* Administrators can suspend pharmacies or products.
* Restricted medicines cannot enter unsupported workflows.
* Support staff can manage complaints.
* Core operational and safety metrics are available.
* Legal, regulatory, privacy, pharmacy, and delivery policies have been reviewed.

---

# 28. Launch Team

A practical pilot team would include:

* Product manager.
* Backend engineer.
* Web or mobile engineer.
* UI/UX designer.
* QA engineer.
* DevOps or cloud engineer.
* Pharmacy operations manager.
* Licensed pharmacist or pharmaceutical advisor.
* Legal and regulatory advisor.
* Customer-support representative.
* Pharmacy partnership representative.

The pharmacist and legal/regulatory advisor should be involved before development decisions are finalized, not only before launch.

---

# 29. Recommended Launch Decision

Proceed with the idea only after repositioning it as:

> A technology platform for medicine discovery, pharmacy offers, prescription-controlled fulfillment, and patient assistance through licensed partners.

Do not launch the original customer-to-customer medicine sale or donation workflow without written advice from qualified Egyptian pharmaceutical and legal specialists and any required regulatory approvals.

The recommended first product is:

# دواك — Medicine Request Marketplace

### Initial promise

“ابعت طلب الدواء مرة واحدة، واستقبل عروض التوفر من صيدليات موثوقة قريبة منك.”

### Initial commercial model

* Free for patients.
* Free pharmacy pilot.
* Pharmacy subscription after validation.
* Optional order, delivery, or technology fee subject to legal review.

### Initial geographical scope

One Cairo or Giza pilot area with 20–50 verified pharmacy branches.

### Initial success target

Demonstrate that the service can:

* Provide an availability response for most supported requests.
* Deliver the first pharmacy response quickly.
* Complete orders safely.
* Generate repeat patient usage.
* Provide measurable new sales opportunities to pharmacies.

---

# 30. Final Product Statement

دواك is not a place where individuals trade medicines.

It is a trusted coordination platform where patients locate medicines, licensed pharmacies respond to requests, pharmacists manage prescription requirements, and approved partners support patients who need financial assistance.

This positioning creates a more scalable, trustworthy, and defensible healthcare business while preserving the strongest part of the original Bziada idea: helping available medicine reach the person who needs it.
