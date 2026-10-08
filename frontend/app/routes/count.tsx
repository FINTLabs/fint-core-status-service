import { statusApi } from "~/api/client.server";
import type { CountResponse } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import type { Route } from "./+types/count";

const KINDS: Record<string, string> = {
  events: "/events/count",
  syncer: "/syncs/count",
};

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  const path = KINDS[params.kind];
  if (!path) throw new Response("Not found", { status: 404 });
  const query = Object.fromEntries(new URL(request.url).searchParams);
  return statusApi.get<CountResponse>(request, env.key, path, query);
}
