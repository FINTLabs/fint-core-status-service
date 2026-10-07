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
