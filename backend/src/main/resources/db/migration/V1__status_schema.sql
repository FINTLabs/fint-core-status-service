drop table if exists request_fint_event_entity;
drop table if exists response_fint_event_entity;
drop table if exists sync_entity;

create table contract (
    id                     bigint generated always as identity primary key,
    username               text        not null,
    org_id                 text        not null,
    adapter_id             text        not null,
    heartbeat_interval_min integer     not null,
    registered_at          timestamptz not null,
    unique (username, org_id)
);

create table heartbeat (
    username     text        not null,
    org_id       text        not null,
    adapter_id   text        not null,
    last_seen_at timestamptz not null,
    primary key (username, org_id)
);

create table capability (
    id                          bigint generated always as identity primary key,
    contract_id                 bigint      not null references contract (id) on delete cascade,
    domain_name                 text        not null,
    package_name                text        not null,
    resource_name               text        not null,
    full_sync_interval_days     integer     not null,
    delta_sync_interval         text,
    unique (contract_id, domain_name, package_name, resource_name)
);

create table sync (
    corr_id           text        primary key,
    sync_type         text        not null,
    adapter_id        text        not null,
    org_id            text        not null,
    domain_name       text        not null,
    package_name      text        not null,
    resource_name     text        not null,
    total_size        bigint      not null,
    total_pages       integer     not null,
    pages_received    integer     not null,
    entities_received bigint      not null,
    started_at        timestamptz not null,
    last_page_at      timestamptz not null,
    completed_at      timestamptz
);

create index sync_type_started_idx on sync (sync_type, started_at desc);
create index sync_resource_started_idx on sync (org_id, domain_name, package_name, resource_name, sync_type, started_at desc);
create index sync_started_idx on sync (started_at);

create table sync_page (
    corr_id     text        not null references sync (corr_id) on delete cascade,
    page        integer     not null,
    page_size   integer     not null,
    received_at timestamptz not null,
    primary key (corr_id, page)
);

create table event (
    corr_id         text        primary key,
    org_id          text        not null,
    domain_name     text,
    package_name    text,
    resource_name   text,
    operation_type  text,
    created_at      timestamptz,
    expires_at      timestamptz,
    status          text        not null,
    adapter_id      text,
    handled_at      timestamptz,
    reason          text,
    received_at     timestamptz not null
);

create index event_status_created_idx on event (status, created_at desc);
create index event_org_created_idx on event (org_id, created_at desc);
create index event_received_idx on event (received_at);
