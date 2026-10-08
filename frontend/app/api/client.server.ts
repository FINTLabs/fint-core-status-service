import { data } from "react-router";
import type { EnvKey } from "~/lib/env";

const BACKENDS: Record<EnvKey, string> = {
  api: "https://core-status.fintlabs.no",
  beta: "https://core-status-beta.fintlabs.no",
  alpha: "https://core-status-alpha.fintlabs.no",
};

type Params = Record<string, string | number | null | undefined>;

function backendUrl(env: EnvKey, path: string, params?: Params): string {
  const base = process.env.STATUS_BACKEND_URL ?? BACKENDS[env];
  const url = new URL(`/api/v1${path}`, base);
  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "")
      url.searchParams.set(key, String(value));
  });
  return url.toString();
}

function headers(request: Request): HeadersInit {
  const authorization = request.headers.get("Authorization");
  return {
    Accept: "application/json",
    "Content-Type": "application/json",
    ...(authorization ? { Authorization: authorization } : {}),
  };
}

async function send<T>(
  request: Request,
  env: EnvKey,
  method: string,
  path: string,
  params?: Params,
  body?: unknown,
): Promise<T> {
  const response = await fetch(backendUrl(env, path, params), {
    method,
    headers: headers(request),
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(10_000),
  });
  if (!response.ok) {
    throw data(
      { message: `${method} ${path} svarte ${response.status}` },
      { status: response.status === 404 ? 404 : 502 },
    );
  }
  return response.status === 204
    ? (undefined as T)
    : ((await response.json()) as T);
}

export const statusApi = {
  get: <T>(request: Request, env: EnvKey, path: string, params?: Params) =>
    send<T>(request, env, "GET", path, params),
  put: <T>(request: Request, env: EnvKey, path: string, body: unknown) =>
    send<T>(request, env, "PUT", path, undefined, body),
  delete: (request: Request, env: EnvKey, path: string) =>
    send<void>(request, env, "DELETE", path),
};
