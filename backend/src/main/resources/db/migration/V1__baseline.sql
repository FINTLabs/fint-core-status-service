create table if not exists request_fint_event_entity (
    corr_id varchar(255) not null primary key,
    created bigint not null,
    time_to_live bigint not null,
    domain_name varchar(255),
    operation_type varchar(255),
    org_id varchar(255),
    package_name varchar(255),
    resource_name varchar(255),
    topic varchar(255),
    value varchar(255)
);

create table if not exists response_fint_event_entity (
    corr_id varchar(255) not null primary key,
    conflicted boolean not null,
    failed boolean not null,
    rejected boolean not null,
    handled_at bigint not null,
    adapter_id varchar(255),
    conflict_reason varchar(255),
    error_message varchar(255),
    org_id varchar(255),
    reject_reason varchar(255),
    sync_page_entry_identifier varchar(255),
    sync_page_entry_resource varchar(255),
    topic varchar(255)
);

create table if not exists sync_entity (
    corr_id varchar(255) not null primary key,
    finished boolean not null,
    sync_type smallint check (sync_type between 0 and 2),
    entities_aquired bigint not null,
    pages_acquired bigint not null,
    saved_at_time_stamp bigint not null,
    total_entities bigint not null,
    total_pages bigint not null,
    adapter_id varchar(255),
    domain varchar(255),
    org_id varchar(255),
    package varchar(255),
    resource varchar(255),
    pages jsonb
);
