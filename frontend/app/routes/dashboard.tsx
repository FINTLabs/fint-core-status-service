import { BodyShort, Heading, Search, Table } from "@navikt/ds-react";
import { ChevronRightIcon } from "@navikt/aksel-icons";
import { useState } from "react";
import { Link } from "react-router";
import { statusApi } from "~/api/client.server";
import type { Dashboard, OrgHealth } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import { useAutoRefresh } from "~/lib/useAutoRefresh";
import { StatusDot, type Tone } from "~/components/StatusDot";
import type { Route } from "./+types/dashboard";

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  return {
    env,
    dashboard: await statusApi.get<Dashboard>(request, env.key, "/dashboard"),
  };
}

function joinParts(parts: (string | false)[]): string {
  return parts.filter(Boolean).join(", ") || "ok";
}

function cells(org: OrgHealth) {
  const heartbeat: [Tone, string] = [
    org.heartbeatStopped ? "bad" : org.heartbeatNever ? "warn" : "ok",
    joinParts([
      org.heartbeatStopped > 0 && `${org.heartbeatStopped} stoppet`,
      org.heartbeatNever > 0 && `${org.heartbeatNever} aldri`,
    ]),
  ];
  const fullSync: [Tone, string] = [
    org.fullSyncOverdue || org.fullSyncNever ? "bad" : "ok",
    joinParts([
      org.fullSyncOverdue > 0 && `${org.fullSyncOverdue} over intervall`,
      org.fullSyncNever > 0 && `${org.fullSyncNever} aldri`,
    ]),
  ];
  const events: [Tone, string] = [
    org.eventsExpired ? "bad" : org.eventsPendingLong ? "warn" : "ok",
    joinParts([
      org.eventsExpired > 0 && `${org.eventsExpired} utløpt`,
      org.eventsPendingLong > 0 && `${org.eventsPendingLong} venter > 5 min`,
    ]),
  ];
  return { heartbeat, fullSync, events };
}

function OrgRow({
  org,
  slug,
  sub,
  open,
  onToggle,
}: {
  org: OrgHealth;
  slug: string;
  sub?: boolean;
  open?: boolean;
  onToggle?: () => void;
}) {
  const { heartbeat, fullSync, events } = cells(org);
  const base = `/${slug}`;
  const q = encodeURIComponent(org.orgId);
  const cellLink = "inline-flex text-inherit no-underline hover:underline";
  return (
    <Table.Row className={sub ? "bg-[var(--ax-bg-neutral-soft)]" : undefined}>
      <Table.DataCell style={sub ? { paddingLeft: 52 } : undefined}>
        <span className="inline-flex items-center gap-1.5">
          {!sub && onToggle ? (
            <button
              type="button"
              aria-expanded={open}
              aria-label={`${open ? "Skjul" : "Vis"} org under ${org.orgId}`}
              onClick={onToggle}
              className="inline-flex h-5 w-5 items-center justify-center rounded bg-transparent p-0 hover:bg-[var(--ax-bg-neutral-soft)]"
            >
              <ChevronRightIcon
                aria-hidden
                className={`transition-transform ${open ? "rotate-90" : ""}`}
              />
            </button>
          ) : !sub ? (
            <span className="inline-block w-5" />
          ) : null}
          <Link
            to={`${base}/kontrakter?sok=${q}`}
            className={sub ? "font-normal" : "font-semibold"}
          >
            {org.orgId}
          </Link>
        </span>
      </Table.DataCell>
      <Table.DataCell>
        {org.contracts}
        {org.muted ? (
          <span className="ml-1.5 text-xs text-[var(--ax-text-neutral-subtle)]">
            {org.muted} dempet
          </span>
        ) : null}
      </Table.DataCell>
      <Table.DataCell>
        <Link
          className={cellLink}
          to={`${base}/kontrakter?sok=${q}${org.heartbeatStopped ? "&vis=heartbeat-stopped" : org.heartbeatNever ? "&vis=heartbeat-never" : ""}`}
        >
          <StatusDot tone={heartbeat[0]}>{heartbeat[1]}</StatusDot>
        </Link>
      </Table.DataCell>
      <Table.DataCell>
        <Link
          className={cellLink}
          to={`${base}/kontrakter?sok=${q}${org.fullSyncOverdue ? "&vis=full-sync-overdue" : org.fullSyncNever ? "&vis=full-sync-never" : ""}`}
        >
          <StatusDot tone={fullSync[0]}>{fullSync[1]}</StatusDot>
        </Link>
      </Table.DataCell>
      <Table.DataCell>
        <Link
          className={cellLink}
          to={`${base}/events?org=${q}${org.eventsExpired ? "&status=expired" : org.eventsPendingLong ? "&status=pending" : ""}`}
        >
          <StatusDot tone={events[0]}>{events[1]}</StatusDot>
        </Link>
      </Table.DataCell>
    </Table.Row>
  );
}

export default function DashboardPage({ loaderData }: Route.ComponentProps) {
  const { env, dashboard } = loaderData;
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState<Record<string, boolean>>({});
  useAutoRefresh(30_000);

  const q = query.trim().toLowerCase();
  const orgs = dashboard.orgs.filter(
    (org) =>
      !q ||
      org.orgId.includes(q) ||
      org.subOrgs.some((s) => s.orgId.includes(q)),
  );
  const { totals } = dashboard;
  const problems = totals.orgsWithProblems;

  return (
    <div className="mx-auto flex w-full max-w-[1000px] flex-col gap-3">
      <div className="flex items-center gap-2.5">
        <StatusDot tone={problems ? "bad" : "ok"} />
        <Heading size="medium" level="1">
          {problems === 0
            ? `${env.label}: alt ser bra ut`
            : `${env.label}: ${problems} ${problems === 1 ? "org trenger" : "org trenger"} oppmerksomhet`}
        </Heading>
      </div>
      <section className="overflow-hidden rounded-lg border border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)]">
        <div className="flex flex-wrap items-center justify-between gap-2 px-3 pt-2.5 pb-1.5">
          <BodyShort
            size="small"
            className="text-[var(--ax-text-neutral-subtle)]"
          >
            {dashboard.orgs.length} org
            {problems
              ? ` · ${problems} med problemer, vist øverst`
              : " · alle ok"}
          </BodyShort>
          <div className="w-56">
            <Search
              label="Søk på org"
              hideLabel
              size="small"
              variant="simple"
              value={query}
              onChange={setQuery}
              onClear={() => setQuery("")}
            />
          </div>
        </div>
        <div className="overflow-x-auto">
          <Table size="small">
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell>Org</Table.HeaderCell>
                <Table.HeaderCell>Kontrakter</Table.HeaderCell>
                <Table.HeaderCell>
                  Heartbeat
                  {totals.heartbeatStopped
                    ? ` · ${totals.heartbeatStopped} stoppet`
                    : ""}
                </Table.HeaderCell>
                <Table.HeaderCell>
                  Fullsync
                  {totals.fullSyncOverdue + totals.fullSyncNever
                    ? ` · ${totals.fullSyncOverdue + totals.fullSyncNever} med problemer`
                    : ""}
                </Table.HeaderCell>
                <Table.HeaderCell>
                  Events
                  {totals.eventsExpired
                    ? ` · ${totals.eventsExpired} utløpt`
                    : ""}
                  {totals.eventsPendingLong
                    ? ` · ${totals.eventsPendingLong} venter`
                    : ""}
                </Table.HeaderCell>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {orgs.map((org) => {
                const hasSubs = org.subOrgs.length > 0;
                const isOpen =
                  hasSubs &&
                  (open[org.orgId] || (!!q && !org.orgId.includes(q)));
                return [
                  <OrgRow
                    key={org.orgId}
                    org={org}
                    slug={env.slug}
                    open={isOpen}
                    onToggle={
                      hasSubs
                        ? () => setOpen((o) => ({ ...o, [org.orgId]: !isOpen }))
                        : undefined
                    }
                  />,
                  ...(isOpen
                    ? org.subOrgs.map((sub) => (
                        <OrgRow
                          key={`${org.orgId}/${sub.orgId}`}
                          org={sub}
                          slug={env.slug}
                          sub
                        />
                      ))
                    : []),
                ];
              })}
            </Table.Body>
          </Table>
          {orgs.length === 0 ? (
            <BodyShort
              size="small"
              className="p-4 text-[var(--ax-text-neutral-subtle)]"
            >
              Ingen org passer søket.
            </BodyShort>
          ) : null}
        </div>
      </section>
    </div>
  );
}
