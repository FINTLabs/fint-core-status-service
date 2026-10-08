import type { ShouldRevalidateFunctionArgs } from "react-router";
import { data, NavLink, Outlet, useLocation, useNavigate } from "react-router";
import { BodyShort, Heading } from "@navikt/ds-react";
import { NovariHeader } from "novari-frontend-components";
import { statusApi } from "~/api/client.server";
import type { Dashboard, Info } from "~/api/types";
import { ENVIRONMENTS } from "~/lib/env";
import { envCookie, requireEnvironment } from "~/lib/env.server";
import { StatusDot } from "~/components/StatusDot";
import type { Route } from "./+types/env-layout";

interface EnvStatus {
  slug: string;
  label: string;
  enabled: boolean;
  problems: number | null;
}

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  const statuses: EnvStatus[] = await Promise.all(
    ENVIRONMENTS.map(async (e) => {
      try {
        const info = await statusApi.get<Info>(request, e.key, "/info");
        if (!info.enabled) {
          return {
            slug: e.slug,
            label: e.label,
            enabled: false,
            problems: null,
          };
        }
        const dashboard = await statusApi.get<Dashboard>(
          request,
          e.key,
          "/dashboard",
        );
        return {
          slug: e.slug,
          label: e.label,
          enabled: true,
          problems: dashboard.totals.orgsWithProblems,
        };
      } catch {
        return { slug: e.slug, label: e.label, enabled: true, problems: null };
      }
    }),
  );
  return data(
    { env, statuses },
    { headers: { "Set-Cookie": await envCookie.serialize(env.slug) } },
  );
}

export function shouldRevalidate({
  currentParams,
  nextParams,
  formMethod,
  defaultShouldRevalidate,
}: ShouldRevalidateFunctionArgs) {
  if (currentParams.env !== nextParams.env) return true;
  if (formMethod && formMethod !== "GET") return defaultShouldRevalidate;
  return false;
}

const SECTIONS = [
  { path: "", label: "Oversikt" },
  { path: "kontrakter", label: "Kontrakter" },
  { path: "syncer", label: "Syncer" },
  { path: "events", label: "Events" },
];

export default function EnvLayout({ loaderData }: Route.ComponentProps) {
  const { env, statuses } = loaderData;
  const current = statuses.find((s) => s.slug === env.slug);
  const navigate = useNavigate();
  const location = useLocation();
  const section = location.pathname.split("/")[2] ?? "";

  return (
    <div className="flex min-h-screen flex-col bg-[var(--ax-bg-neutral-soft)]">
      <NovariHeader
        appName="FINT Core Status"
        showLogoWithTitle
        isLoggedIn
        displayName=""
        onLogin={() => {}}
        onLogout={() => {}}
        menu={SECTIONS.map((s) => ({
          label: s.label,
          action: `/${env.slug}${s.path ? `/${s.path}` : ""}`,
        }))}
        onMenuClick={(action) => navigate(action)}
      >
        <nav aria-label="Miljø" className="flex flex-wrap items-center gap-1.5">
          {statuses.map((s) =>
            s.enabled ? (
              <NavLink
                key={s.slug}
                to={`/${s.slug}${section ? `/${section}` : ""}`}
                className={({ isActive }) =>
                  `inline-flex min-h-8 items-center rounded-full border px-3 text-sm font-semibold no-underline ${
                    isActive || s.slug === env.slug
                      ? "border-[var(--ax-text-neutral)] bg-[var(--ax-text-neutral)] text-[var(--ax-bg-default)]"
                      : "border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)] text-[var(--ax-text-neutral)]"
                  }`
                }
              >
                <StatusDot
                  tone={
                    s.problems === null
                      ? "muted"
                      : s.problems > 0
                        ? "bad"
                        : "ok"
                  }
                >
                  {s.label}
                  <span className="font-normal opacity-80">
                    {s.problems === null
                      ? "ukjent"
                      : s.problems > 0
                        ? `${s.problems} org`
                        : "ok"}
                  </span>
                </StatusDot>
              </NavLink>
            ) : (
              <span
                key={s.slug}
                aria-disabled="true"
                title={`${s.label} er ikke aktivert`}
                className="inline-flex min-h-8 cursor-not-allowed items-center rounded-full border border-dashed border-[var(--ax-border-neutral-subtle)] px-3 text-sm font-semibold text-[var(--ax-text-neutral-subtle)]"
              >
                <StatusDot tone="muted">
                  {s.label}
                  <span className="font-normal">ikke aktivert</span>
                </StatusDot>
              </span>
            ),
          )}
        </nav>
      </NovariHeader>
      <main className="mx-auto box-border flex w-full max-w-[1360px] flex-1 flex-col gap-4 px-6 pt-4 pb-6">
        {current && !current.enabled ? (
          <div className="mx-auto mt-16 flex max-w-xl flex-col items-center gap-2 text-center">
            <Heading size="medium" level="1">
              {env.label} er ikke aktivert
            </Heading>
            <BodyShort className="text-[var(--ax-text-neutral-subtle)]">
              Status-tjenesten leser ikke fint-core i {env.label} ennå. Den
              skrus på med fint.status.enabled i backend/kustomize/{env.key}.
            </BodyShort>
          </div>
        ) : (
          <Outlet />
        )}
      </main>
    </div>
  );
}
