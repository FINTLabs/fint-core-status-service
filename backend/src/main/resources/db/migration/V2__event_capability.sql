create table event_capability (
    contract_id   bigint not null references contract (id) on delete cascade,
    domain_name   text   not null,
    package_name  text   not null,
    resource_name text   not null,
    operations    text[] not null,
    primary key (contract_id, domain_name, package_name, resource_name)
);
