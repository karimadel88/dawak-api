create table city (
    id uuid primary key,
    code varchar(50) not null unique,
    name_ar varchar(120) not null,
    name_en varchar(120) not null,
    active boolean not null default true
);

create table area (
    id uuid primary key,
    city_id uuid not null references city(id),
    code varchar(50) not null,
    name_ar varchar(120) not null,
    name_en varchar(120) not null,
    active boolean not null default true,
    unique (city_id, code),
    unique (id, city_id)
);

create table app_user (
    id uuid primary key,
    phone_number varchar(20) not null unique,
    phone_number_verified_at timestamptz not null,
    email varchar(254),
    email_verified_at timestamptz,
    status varchar(40) not null,
    preferred_language varchar(5) not null default 'ar',
    last_login_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_app_user_language check (preferred_language in ('ar', 'en'))
);

create table patient_profile (
    id uuid primary key,
    user_id uuid not null unique references app_user(id),
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    birth_year integer,
    city_id uuid not null,
    area_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    foreign key (area_id, city_id) references area(id, city_id),
    constraint ck_patient_birth_year check (birth_year is null or birth_year between 1900 and 2100)
);

create table consent_record (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    consent_type varchar(60) not null,
    document_version varchar(40) not null,
    status varchar(30) not null,
    granted_at timestamptz not null,
    withdrawn_at timestamptz,
    source varchar(40) not null,
    ip_address varchar(64),
    user_agent varchar(500),
    created_at timestamptz not null,
    unique (user_id, consent_type, document_version, granted_at)
);

create table otp_challenge (
    id uuid primary key,
    phone_number varchar(20) not null,
    code_hash varchar(64) not null,
    expires_at timestamptz not null,
    attempt_count integer not null default 0,
    max_attempts integer not null,
    consumed_at timestamptz,
    request_ip varchar(64),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_otp_attempt_count check (attempt_count >= 0 and max_attempts > 0)
);

create index ix_otp_phone_created on otp_challenge(phone_number, created_at desc);

create table auth_session (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    refresh_token_hash varchar(64) not null unique,
    device_id varchar(200) not null,
    device_name varchar(200),
    ip_address varchar(64),
    user_agent varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    last_used_at timestamptz not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    version bigint not null default 0
);

create index ix_auth_session_user on auth_session(user_id, created_at desc);
create unique index ux_auth_session_active_device on auth_session(user_id, device_id) where revoked_at is null;

create table audit_event (
    id uuid primary key,
    actor_user_id uuid references app_user(id),
    event_type varchar(100) not null,
    aggregate_type varchar(80) not null,
    aggregate_id uuid,
    outcome varchar(30) not null,
    correlation_id varchar(100),
    ip_address varchar(64),
    user_agent varchar(500),
    metadata text,
    created_at timestamptz not null
);

create index ix_audit_event_actor_created on audit_event(actor_user_id, created_at desc);
create index ix_audit_event_type_created on audit_event(event_type, created_at desc);

insert into city (id, code, name_ar, name_en) values
    ('10000000-0000-0000-0000-000000000001', 'CAIRO', 'القاهرة', 'Cairo'),
    ('10000000-0000-0000-0000-000000000002', 'GIZA', 'الجيزة', 'Giza');

insert into area (id, city_id, code, name_ar, name_en) values
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'NASR_CITY', 'مدينة نصر', 'Nasr City'),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'MAADI', 'المعادي', 'Maadi'),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'DOKKI', 'الدقي', 'Dokki');
