# Status service frontend

React Router app (server-rendered) with Aksel and Novari components. It shows the four screens from the status service: Oversikt, Kontrakter, Syncer and Events.

## How it talks to the backend

- All backend calls happen in loaders and actions on the server, in `app/api/client.server.ts`. The browser never calls the backend directly.
- The token comes from the `Authorization` header that the SSO middleware puts on each incoming request, and is passed on for that request only.
- Each environment has its own backend. The environment is the first part of the path: `/prod` (the api environment), `/beta` and `/alpha`. `api` cannot be used in the path, because Traefik sends `/api` on core-status.fintlabs.no to the backend.
- Filters and paging live in the URL, so a filtered view can be shared as a link.

## API types

`app/api/openapi.json` is written by the backend's `OpenApiSpecTest`. Generate the types from it after a backend change:

```bash
npm run api:types
```

CI fails if `openapi.json` or `schema.ts` is out of date.

## Running locally

```bash
npm ci
STATUS_BACKEND_URL=http://localhost:8080 npm run dev
```

`STATUS_BACKEND_URL` points every environment at one backend. Without it the real backends are used, which need a valid token.

## Checks

```bash
npm run typecheck
npm run lint
npm run build
```
