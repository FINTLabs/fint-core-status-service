import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/dashboard.tsx"),

  // route("dashboard", "routes/dashboard.tsx"),

  route("adaptere", "routes/adapter.tsx"),

  // contract/$orgId/$componentId
  route(
    "contract/:orgId/:componentId",
    "routes/contract.$orgId.$componentId.tsx",
  ),

  // adaptere/$orgId/$domain
  route("adaptere/:orgId/:domain", "routes/adapter.$orgId.$domain.tsx"),

  route("hendelser", "routes/events.tsx"),
  route("sync", "routes/sync.tsx"),
] satisfies RouteConfig;
