# FINT Core Status Service

Shows the health of FINT adapters: contracts, heartbeats, syncs and request/response events.

| Directory | What | Image |
|---|---|---|
| [`backend/`](backend) | Spring Boot (Kotlin) service that reads the fint-core status topics | `ghcr.io/fintlabs/fint-core-status-service` |
| [`frontend/`](frontend) | React Router app that shows the status | `ghcr.io/fintlabs/fint-core-status-service-frontend` |

The frontend was moved here from `FINTLabs/fint-core-status-service-frontend` with its git history kept.

## Build and deploy

Each part has its own workflow and only runs when its directory changes:

- `.github/workflows/backend.yaml`: builds and tests on pull requests, deploys to beta on `main`.
- `.github/workflows/frontend.yaml`: builds on pull requests, deploys to api on `main`.
- `.github/workflows/backend-manual-deploy.yaml`: deploys the backend to a chosen cluster by hand.

See [`backend/README.md`](backend/README.md) and [`frontend/README.md`](frontend/README.md) for running each part locally.
