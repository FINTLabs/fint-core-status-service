create sequence hibernate_sequence start 1 increment 1;
create table adapter_capability
(
    id                          varchar(255) not null,
    delta_sync_interval         int4,
    domain_name                 varchar(255),
    full_sync_interval_in_days  int4         not null,
    package_name                varchar(255),
    resource_name               varchar(255),
    adapter_contract_adapter_id varchar(255),
    primary key (id)
);
create table adapter_contract
(
    adapter_id                    varchar(255) not null,
    heartbeat_interval_in_minutes int4         not null,
    last_seen                     int8         not null,
    org_id                        varchar(255),
    time                          int8         not null,
    username                      varchar(255),
    primary key (adapter_id)
);
create table adapter_delete_sync
(
    id          int8 not null,
    adapter_id  varchar(255),
    corr_id     varchar(255),
    org_id      varchar(255),
    page        int8 not null,
    page_size   int8 not null,
    time        int8 not null,
    total_pages int8 not null,
    total_size  int8 not null,
    uri_ref     varchar(255),
    primary key (id)
);
create table adapter_full_sync
(
    id          int8 not null,
    adapter_id  varchar(255),
    corr_id     varchar(255),
    org_id      varchar(255),
    page        int8 not null,
    page_size   int8 not null,
    time        int8 not null,
    total_pages int8 not null,
    total_size  int8 not null,
    uri_ref     varchar(255),
    primary key (id)
);
create table consumer_event_request
(
    id            int8 not null,
    corr_id       varchar(255),
    created       int8 not null,
    domain_name   varchar(255),
    operation     int4,
    org_id        varchar(255),
    package_name  varchar(255),
    resource_name varchar(255),
    time_to_live  int8 not null,
    primary key (id)
);
create table consumer_event_response
(
    id            int8    not null,
    adapter_id    varchar(255),
    corr_id       varchar(255),
    error_message varchar(255),
    failed        boolean not null,
    handled_at    int8    not null,
    org_id        varchar(255),
    reject_reason varchar(255),
    rejected      boolean not null,
    primary key (id)
);
alter table adapter_capability
    add constraint FKkfcnl1ws00dr86bnc61vqj4um foreign key (adapter_contract_adapter_id) references adapter_contract;
