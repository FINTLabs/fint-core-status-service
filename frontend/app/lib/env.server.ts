import { createCookie, data } from "react-router";
import { type Environment, environmentBySlug } from "./env";

export const envCookie = createCookie("statusEnv", {
  maxAge: 60 * 60 * 24 * 30,
  httpOnly: true,
  secure: true,
  path: "/",
  sameSite: "lax",
});

export function requireEnvironment(slug: string | undefined): Environment {
  const env = environmentBySlug(slug);
  if (!env) throw data({ message: `Ukjent miljø: ${slug}` }, { status: 404 });
  return env;
}
