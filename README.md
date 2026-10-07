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
- **CD** (`cd.yml`) runs on `main` (api) and `develop` (beta). Each image is tagged `sha-<last commit that changed the code>`, so a change to `kustomize/` or a README only redeploys the image that is already there, without a new build. The paths that count as code are listed in `.github/scripts/paths.sh`.
- A part is only deployed to an environment it has an overlay for, for example `backend/kustomize/beta`. The frontend only runs in api.
- `backend-manual-deploy.yaml` deploys the backend to a chosen cluster by hand, which is how alpha is deployed.

See [`backend/README.md`](backend/README.md) and [`frontend/README.md`](frontend/README.md) for running each part locally.
