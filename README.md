# FINT Core Status Service

Shows the health of FINT adapters: contracts, heartbeats, syncs and request/response events.

| Directory | What | Image |
|---|---|---|
| [`backend/`](backend) | Spring Boot (Kotlin) service that reads the fint-core status topics | `ghcr.io/fintlabs/fint-core-status-service` |
| [`frontend/`](frontend) | React Router app that shows the status | `ghcr.io/fintlabs/fint-core-status-service-frontend` |

The frontend was moved here from `FINTLabs/fint-core-status-service-frontend` with its git history kept.

## Build and deploy

The setup follows fint-core:

- **CI** (`ci.yml`) checks only the parts whose code changed.
- **CD** (`cd.yml`) runs on `main` and deploys every part to every environment it has an overlay for: the backend to alpha, beta and api, the frontend to api. Each image is tagged `sha-<last commit that changed the code>`, so a change to `kustomize/` or a README only redeploys the image that is already there, without a new build. The paths that count as code are listed in `.github/scripts/paths.sh`.
- There is no develop branch. Changes are checked in the pull request before they are merged.
- `backend-manual-deploy.yaml` deploys the backend to a chosen cluster by hand.

See [`backend/README.md`](backend/README.md) and [`frontend/README.md`](frontend/README.md) for running each part locally.
