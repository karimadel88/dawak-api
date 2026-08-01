create table pharmacy (
    id uuid primary key,
    public_name varchar(200) not null,
    license_number varchar(100) not null unique,
    license_expiry_date date not null,
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_pharmacy_status check (status in
        ('DRAFT','PENDING_REVIEW','APPROVED','REJECTED','SUSPENDED','DOCUMENTS_EXPIRED','CLOSED'))
);

alter table prescription add column medicine_package_id uuid references medicine_package(id);
create index ix_prescription_medicine_package on prescription(medicine_package_id);

create table pharmacy_branch (
    id uuid primary key,
    pharmacy_id uuid not null references pharmacy(id),
    branch_code varchar(80) not null,
    name varchar(200) not null,
    status varchar(40) not null,
    city_id uuid not null references city(id),
    area_id uuid not null references area(id),
    pickup_enabled boolean not null default true,
    delivery_enabled boolean not null default false,
    prescription_handling_enabled boolean not null default false,
    accepting_requests boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    unique (pharmacy_id, branch_code),
    constraint ck_pharmacy_branch_status check (status in
        ('PENDING_APPROVAL','ACTIVE','TEMPORARILY_INACTIVE','SUSPENDED','CLOSED'))
);

create table pharmacy_branch_service_area (
    pharmacy_branch_id uuid not null references pharmacy_branch(id) on delete cascade,
    area_id uuid not null references area(id),
    distance_km numeric(6,2) not null,
    primary key (pharmacy_branch_id, area_id),
    constraint ck_branch_service_distance check (distance_km >= 0)
);

create table medicine_request (
    id uuid primary key,
    reference_number varchar(40) not null unique,
    patient_profile_id uuid not null references patient_profile(id),
    prescription_id uuid references prescription(id),
    city_id uuid not null references city(id),
    area_id uuid not null references area(id),
    fulfillment_preference varchar(20) not null,
    urgency varchar(20) not null,
    status varchar(50) not null,
    search_radius_km integer not null,
    submitted_at timestamptz,
    matching_started_at timestamptz,
    expires_at timestamptz,
    cancel_reason_code varchar(80),
    cancel_reason_text varchar(500),
    cancelled_by_user_id uuid references app_user(id),
    cancelled_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_request_fulfillment check (fulfillment_preference in ('PICKUP','DELIVERY','EITHER')),
    constraint ck_request_urgency check (urgency in ('NORMAL','URGENT')),
    constraint ck_request_radius check (search_radius_km > 0)
);

create table medicine_request_item (
    id uuid primary key,
    medicine_request_id uuid not null unique references medicine_request(id) on delete cascade,
    medicine_package_id uuid not null references medicine_package(id),
    requested_quantity integer not null,
    patient_notes varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_request_item_quantity check (requested_quantity > 0)
);

create table medicine_request_status_history (
    id uuid primary key,
    medicine_request_id uuid not null references medicine_request(id),
    old_status varchar(50),
    new_status varchar(50) not null,
    actor_user_id uuid references app_user(id),
    reason varchar(500),
    created_at timestamptz not null
);

create table request_pharmacy_match (
    id uuid primary key,
    medicine_request_id uuid not null references medicine_request(id),
    pharmacy_branch_id uuid not null references pharmacy_branch(id),
    distance_km numeric(6,2) not null,
    match_score numeric(8,2) not null,
    match_reason varchar(200) not null,
    notification_status varchar(30) not null,
    viewed_at timestamptz,
    responded_at timestamptz,
    created_at timestamptz not null,
    unique (medicine_request_id, pharmacy_branch_id)
);

create table medicine_request_idempotency_key (
    user_id uuid not null references app_user(id),
    operation varchar(40) not null,
    idempotency_key varchar(120) not null,
    request_hash varchar(64) not null,
    medicine_request_id uuid references medicine_request(id),
    created_at timestamptz not null,
    primary key (user_id, operation, idempotency_key)
);

create index ix_request_patient_created on medicine_request(patient_profile_id, created_at desc);
create index ix_request_status_expiry on medicine_request(status, expires_at);
create index ix_request_location_status on medicine_request(city_id, area_id, status);
create index ix_request_item_package on medicine_request_item(medicine_package_id);
create index ix_request_history_request on medicine_request_status_history(medicine_request_id, created_at);
create index ix_request_match_branch_status on request_pharmacy_match(pharmacy_branch_id, notification_status);
create index ix_branch_eligibility on pharmacy_branch(city_id, status, accepting_requests);
