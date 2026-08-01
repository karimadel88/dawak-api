create extension if not exists pg_trgm;

create table manufacturer (
    id uuid primary key,
    code varchar(60) not null unique,
    name_ar varchar(160) not null,
    name_en varchar(160) not null,
    normalized_name varchar(320) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table active_ingredient (
    id uuid primary key,
    code varchar(80) not null unique,
    name_ar varchar(160) not null,
    name_en varchar(160) not null,
    normalized_name varchar(320) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table dosage_form (
    id uuid primary key,
    code varchar(80) not null unique,
    name_ar varchar(120) not null,
    name_en varchar(120) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table medicine (
    id uuid primary key,
    name_ar varchar(200) not null,
    name_en varchar(200) not null,
    normalized_name_ar varchar(240) not null,
    normalized_name_en varchar(240) not null,
    manufacturer_id uuid not null references manufacturer(id),
    description text,
    prescription_required boolean not null default false,
    restricted boolean not null default false,
    restriction_code varchar(80),
    storage_type varchar(80),
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_medicine_restriction check (not restricted or restriction_code is not null)
);

create table medicine_active_ingredient (
    medicine_id uuid not null references medicine(id) on delete cascade,
    active_ingredient_id uuid not null references active_ingredient(id),
    sequence_number integer not null,
    primary key (medicine_id, active_ingredient_id),
    unique (medicine_id, sequence_number)
);

create table medicine_package (
    id uuid primary key,
    medicine_id uuid not null references medicine(id),
    strength_value numeric(12, 4) not null,
    strength_unit varchar(40) not null,
    dosage_form_id uuid not null references dosage_form(id),
    package_size_value numeric(12, 4) not null,
    package_size_unit varchar(40) not null,
    route_of_administration varchar(80),
    barcode varchar(80) unique,
    official_price numeric(12, 2),
    currency char(3),
    status varchar(30) not null,
    active boolean not null default true,
    search_text text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint ck_medicine_package_strength check (strength_value > 0),
    constraint ck_medicine_package_size check (package_size_value > 0),
    constraint ck_medicine_package_price check (official_price is null or official_price >= 0),
    constraint ck_medicine_package_currency check ((official_price is null and currency is null) or (official_price is not null and currency is not null)),
    constraint ck_medicine_package_status check (status in ('AVAILABLE', 'UNAVAILABLE', 'RECALLED', 'UNSUPPORTED')),
    unique (medicine_id, strength_value, strength_unit, dosage_form_id, package_size_value, package_size_unit, route_of_administration)
);

create table medicine_alias (
    id uuid primary key,
    medicine_id uuid not null references medicine(id) on delete cascade,
    alias varchar(200) not null,
    normalized_alias varchar(240) not null,
    created_at timestamptz not null,
    unique (medicine_id, normalized_alias)
);

create table catalogue_status_history (
    id uuid primary key,
    medicine_package_id uuid not null references medicine_package(id),
    old_status varchar(30),
    new_status varchar(30) not null,
    old_active boolean,
    new_active boolean not null,
    actor_user_id uuid references app_user(id),
    reason varchar(500),
    created_at timestamptz not null
);

create table catalogue_idempotency_key (
    idempotency_key varchar(120) primary key,
    operation varchar(40) not null,
    request_hash varchar(64) not null,
    result_ids text not null,
    created_at timestamptz not null
);

create index ix_medicine_normalized_ar on medicine using gin (normalized_name_ar gin_trgm_ops);
create index ix_medicine_normalized_en on medicine using gin (normalized_name_en gin_trgm_ops);
create index ix_manufacturer_normalized on manufacturer using gin (normalized_name gin_trgm_ops);
create index ix_active_ingredient_normalized on active_ingredient using gin (normalized_name gin_trgm_ops);
create index ix_medicine_package_search on medicine_package using gin (search_text gin_trgm_ops);
create index ix_medicine_package_status on medicine_package(status, active);
create index ix_catalogue_history_package on catalogue_status_history(medicine_package_id, created_at desc);
