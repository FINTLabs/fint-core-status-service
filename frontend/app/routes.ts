import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route(":env", "routes/env-layout.tsx", [
    index("routes/dashboard.tsx"),
    route("kontrakter", "routes/contracts.tsx", [
      route(":id", "routes/contract.tsx"),
    ]),
    route("syncer", "routes/syncs.tsx", [route(":corrId", "routes/sync.tsx")]),
    route("events", "routes/events.tsx", [
      route(":corrId", "routes/event.tsx"),
    ]),
    route("antall/:kind", "routes/count.tsx"),
  ]),
] satisfies RouteConfig;
