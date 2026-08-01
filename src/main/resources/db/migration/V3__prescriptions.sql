create table prescription (
    id uuid primary key,
    patient_profile_id uuid not null references patient_profile(id),
    storage_key varchar(200) not null unique,
    original_filename varchar(255) not null,
    declared_content_type varchar(100) not null,
    detected_content_type varchar(100),
    file_size bigint not null,
    checksum_sha256 varchar(64) not null,
    status varchar(40) not null,
    upload_token_hash varchar(64),
    upload_expires_at timestamptz,
    uploaded_at timestamptz,
    reviewed_by_user_id uuid references app_user(id),
    reviewed_at timestamptz,
    review_reason_code varchar(80),
    review_comment varchar(500),
    valid_until timestamptz,
    retention_until timestamptz not null,
    deleted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_prescription_file_size check (file_size > 0),
    constraint ck_prescription_checksum check (checksum_sha256 ~ '^[0-9a-f]{64}$')
);

create index ix_prescription_patient_created on prescription(patient_profile_id, created_at desc);
create index ix_prescription_status_created on prescription(status, created_at);
create index ix_prescription_reviewer_status on prescription(reviewed_by_user_id, status);

create table prescription_access_grant (
    id uuid primary key,
    prescription_id uuid not null references prescription(id),
    user_id uuid not null references app_user(id),
    token_hash varchar(64) not null unique,
    purpose varchar(80) not null,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null
);

create index ix_prescription_access_grant_lookup
    on prescription_access_grant(prescription_id, user_id, expires_at);

create table prescription_access_log (
    id uuid primary key,
    prescription_id uuid not null references prescription(id),
    accessed_by_user_id uuid not null references app_user(id),
    access_type varchar(40) not null,
    purpose varchar(80) not null,
    ip_address varchar(64),
    user_agent varchar(500),
    created_at timestamptz not null
);

create index ix_prescription_access_log_prescription_created
    on prescription_access_log(prescription_id, created_at desc);
