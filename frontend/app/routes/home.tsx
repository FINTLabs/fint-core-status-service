import { redirect } from "react-router";
import { environmentBySlug } from "~/lib/env";
import { envCookie } from "~/lib/env.server";
import type { Route } from "./+types/home";

export async function loader({ request }: Route.LoaderArgs) {
  const saved = await envCookie.parse(request.headers.get("Cookie"));
  return redirect(`/${environmentBySlug(saved)?.slug ?? "prod"}`);
}
