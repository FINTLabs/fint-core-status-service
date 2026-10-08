# Status service backend

Reads the status topics that fint-core publishes and stores what it needs in Postgres. The API is served under `/api/v1`.

## Topics

All topics are global, written by every org's adapter gateway (or the client API for requests).

| Topic | What we store |
|---|---|
| `novari-no.fint-core.fint-felleskomponent-adapter-contract` | Contracts and their capabilities, keyed on (username, orgId) |
| `novari-no.fint-core.fint-felleskomponent-adapter-heartbeat` | Last heartbeat per contract, using the Kafka record time |
| `novari-no.fint-core.fint-felleskomponent-adapter-{full,delta,delete}-sync` | One row per sync and one per received page |
| `novari-no.fint-core.fint-felleskomponent-event-request` | Requests, as pending events |
| `novari-no.fint-core.fint-felleskomponent-event-response` | Answers, failures and expiries for those events |

The topics only keep data for one day, so Postgres is where history lives. Events and syncs are kept for 30 days.

## API

Every endpoint needs a valid token. The OpenAPI description is at `/api/v1/api-docs` and Swagger UI at `/api/v1/swagger-ui.html`. Errors are `application/problem+json`. Lists are paged with `page` (from 0) and `size` (default 25, at most 100) and return `total`.

| Endpoint | What |
|---|---|
| `GET /api/v1/dashboard` | One row per main org (summed with its sub-orgs), with a row per member org under it |
| `GET /api/v1/contracts` | `q`, `filter` (all, heartbeat-stopped, heartbeat-never, full-sync-overdue, full-sync-never, muted), `sort` (health, org, username, heartbeat, full-sync, capabilities), `direction` |
| `GET /api/v1/contracts/{id}` | One contract with its capabilities and mute |
| `PUT /api/v1/contracts/{id}/mute` | Mute warnings, with an optional `reason` and `mutedUntil` |
| `DELETE /api/v1/contracts/{id}/mute` | Turn warnings back on |
| `GET /api/v1/syncs` | `type` (full by default), `from` (7 days back by default), `to`, `state`, `org`, `corrId`, `domain`, `package`, `resource` |
| `GET /api/v1/syncs/{corrId}` | One sync with its pages |
| `GET /api/v1/syncs/count?since=` | How many syncs matching the filters started after `since` |
| `GET /api/v1/events` | `status` (pending, expired, answered, errors), `from` (1 day back by default), `to`, `org`, `corrId`, `domain`, `package`, `resource` |
| `GET /api/v1/events/{corrId}` | One event, with the contracts that could have answered it if it is unanswered |
| `GET /api/v1/events/count?since=` | How many events matching the filters arrived after `since` |
| `GET /api/v1/orgs` | Main orgs with their sub-orgs |
| `GET /api/v1/model` | Domains, packages and resources from the information model |

Filtering on an org always includes its sub-orgs.

## Health rules

- A heartbeat has stopped when none has arrived for twice the contract's heartbeat interval.
- A full sync is overdue when the last completed one is older than the capability's interval.
- A capability that has never completed a full sync is a problem once the contract has been registered for longer than that interval.
- A sync without all its pages is stalled when no page has arrived for 3 minutes.
- A muted contract is shown but never counts as a problem, and neither does an expired event that only muted contracts could have answered.

## Metrics

| Metric | Labels | Value |
|---|---|---|
| `fint_status_heartbeat_ok` | org, main_org, username | 1 healthy or muted, 0 stopped |
| `fint_status_full_sync_ok` | org, main_org, username, domain, package, resource | 1 healthy, waiting or muted, 0 overdue or never |
| `fint_status_contract_muted` | org, main_org, username | 1 for each muted contract |
| `fint_status_events_expired` | org, main_org | expired events in the last 24 hours that count as problems |

## Switching an environment on or off

`fint.status.enabled` in `kustomize/<env>/flais.yaml` decides whether the service reads fint-core in that environment. With `"false"` the Kafka listeners do not start, and the frontend greys the environment out. Set it to `"true"` (or remove it) and merge to switch it on. When it comes back on it catches up on what the topics still hold: every contract, and the last day of heartbeats, syncs and events.

## Switching an environment on or off

`fint.status.enabled` in `kustomize/<env>/flais.yaml` decides whether the service reads fint-core in that environment. With `"false"` the Kafka listeners do not start, and the frontend greys the environment out. Set it to `"true"` (or remove it) and merge to switch it on. When it comes back on it catches up on what the topics still hold: every contract, and the last day of heartbeats, syncs and events.

Today it is on in beta and off in alpha and api, which do not run fint-core yet.

## Running locally

Requires Docker. The Java 25 toolchain is downloaded by Gradle if you do not have it.

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Tests

```bash
./gradlew check
```

The integration tests start Postgres and Kafka with Testcontainers and send records shaped like the ones fint-core produces.
