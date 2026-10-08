import type { ShouldRevalidateFunctionArgs } from "react-router";
import { Button, Chips, Select, Table } from "@navikt/ds-react";
import { Outlet, useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type { ModelDomain, OrgNode, SyncPage, SyncSummary } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import { PAGE_SIZE, pageFrom, useFilters } from "~/lib/filters";
import {
  formatDateTime,
  formatDuration,
  formatNumber,
  resourcePath,
} from "~/lib/format";
import { sharedFilterParams, toQueryString } from "~/lib/query";
import { CorrIdField } from "~/components/CorrIdField";
import { FilterBar, FilterRow } from "~/components/FilterBar";
import { NewItemsBanner } from "~/components/NewItemsBanner";
import { OrgSelect } from "~/components/OrgSelect";
import { PageHeading } from "~/components/PageHeading";
import { Pager } from "~/components/Pager";
import { ResourceSelects } from "~/components/ResourceSelects";
import { Mono } from "~/components/Mono";
import { StatusDot, type Tone } from "~/components/StatusDot";
import type { Route } from "./+types/syncs";

const TYPES: [string, string][] = [
  ["full", "Full"],
  ["delta", "Delta"],
  ["delete", "Slett"],
  ["all", "Alle typer"],
];

export const SYNC_STATE: Record<SyncSummary["state"], [Tone, string]> = {
  COMPLETED: ["ok", "Fullført"],
  RUNNING: ["info", "Pågår"],
  STALLED: ["bad", "Stoppet"],
};

export const SYNC_TYPE: Record<SyncSummary["type"], string> = {
  FULL: "Full",
  DELTA: "Delta",
  DELETE: "Slett",
};

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  const search = new URL(request.url).searchParams;
  const page = pageFrom(search);
  const days = Number(search.get("periode") ?? "7") || 7;
  const loadedAt = new Date().toISOString();
  const filters = {
    type: search.get("type") ?? "full",
    state: search.get("status"),
    ...sharedFilterParams(search),
  };
  const [syncs, orgs, model] = await Promise.all([
    statusApi.get<SyncPage>(request, env.key, "/syncs", {
      ...filters,
      from: new Date(Date.now() - days * 86_400_000).toISOString(),
      page: page - 1,
      size: PAGE_SIZE,
    }),
    statusApi.get<OrgNode[]>(request, env.key, "/orgs"),
    statusApi.get<ModelDomain[]>(request, env.key, "/model"),
  ]);
  const countUrl = `/${env.slug}/antall/syncer?${toQueryString({ ...filters, since: loadedAt })}`;
  return { env, syncs, orgs, model, page, countUrl };
}

export function shouldRevalidate({
  currentUrl,
  nextUrl,
  defaultShouldRevalidate,
}: ShouldRevalidateFunctionArgs) {
  if (
    currentUrl.search === nextUrl.search &&
    currentUrl.pathname !== nextUrl.pathname
  )
    return false;
  return defaultShouldRevalidate;
}

export default function SyncsPage({ loaderData }: Route.ComponentProps) {
  const { syncs, orgs, model, page, countUrl } = loaderData;
  const { get, set } = useFilters();
  const navigate = useNavigate();
  const location = useLocation();
  const type = get("type") || "full";

  return (
    <div className="flex flex-col gap-3">
      <PageHeading title="Syncer" detail={`${syncs.total} syncer`} />
      <FilterBar>
        <FilterRow>
          <Chips size="small" aria-label="Synctype">
            {TYPES.map(([key, label]) => (
              <Chips.Toggle
                key={key}
                selected={type === key}
                checkmark={false}
                onClick={() => set({ type: key === "full" ? null : key })}
              >
                {label}
              </Chips.Toggle>
            ))}
          </Chips>
          <Select
            size="small"
            label="Periode"
            value={get("periode") || "7"}
            onChange={(e) =>
              set({ periode: e.target.value === "7" ? null : e.target.value })
            }
          >
            <option value="1">Siste døgn</option>
            <option value="7">Siste 7 dager</option>
            <option value="30">Siste 30 dager</option>
          </Select>
          <Select
            size="small"
            label="Status"
            value={get("status")}
            onChange={(e) => set({ status: e.target.value })}
          >
            <option value="">Alle</option>
            <option value="completed">Fullført</option>
            <option value="running">Pågår</option>
            <option value="stalled">Stoppet</option>
          </Select>
          <OrgSelect orgs={orgs} />
        </FilterRow>
        <FilterRow>
          <CorrIdField />
          <ResourceSelects model={model} />
          <Button
            size="small"
            variant="tertiary"
            onClick={() => navigate({ search: "" })}
          >
            Nullstill
          </Button>
          <NewItemsBanner countUrl={countUrl} label="nye syncer" />
        </FilterRow>
      </FilterBar>
      <section className="overflow-hidden rounded-lg border border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)]">
        <div className="overflow-x-auto">
          <Table size="small">
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell>Startet</Table.HeaderCell>
                <Table.HeaderCell>Type</Table.HeaderCell>
                <Table.HeaderCell>Org</Table.HeaderCell>
                <Table.HeaderCell>Ressurs</Table.HeaderCell>
                <Table.HeaderCell>Sider</Table.HeaderCell>
                <Table.HeaderCell>Entiteter</Table.HeaderCell>
                <Table.HeaderCell>Status</Table.HeaderCell>
                <Table.HeaderCell>Varighet</Table.HeaderCell>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {syncs.items.map((s) => {
                const [tone, label] = SYNC_STATE[s.state];
                const pct = Math.round(
                  (100 * s.pagesReceived) / Math.max(1, s.totalPages),
                );
                return (
                  <Table.Row
                    key={s.corrId}
                    className="cursor-pointer"
                    onClick={() =>
                      navigate(
                        { pathname: s.corrId, search: location.search },
                        { preventScrollReset: true },
                      )
                    }
                  >
                    <Table.DataCell className="whitespace-nowrap">
                      {formatDateTime(s.startedAt)}
                    </Table.DataCell>
                    <Table.DataCell>{SYNC_TYPE[s.type]}</Table.DataCell>
                    <Table.DataCell>{s.orgId}</Table.DataCell>
                    <Table.DataCell>
                      <Mono>
                        {resourcePath(
                          s.domainName,
                          s.packageName,
                          s.resourceName,
                        )}
                      </Mono>
                    </Table.DataCell>
                    <Table.DataCell className="min-w-28">
                      <div className="flex flex-col gap-1">
                        {s.pagesReceived} / {s.totalPages}
                        <div className="h-1.5 overflow-hidden rounded bg-[var(--ax-bg-neutral-soft)]">
                          <div
                            className="h-full bg-[#0056B4]"
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                      </div>
                    </Table.DataCell>
                    <Table.DataCell className="whitespace-nowrap">
                      {formatNumber(s.entitiesReceived)} /{" "}
                      {formatNumber(s.totalSize)}
                    </Table.DataCell>
                    <Table.DataCell>
                      <StatusDot tone={tone}>{label}</StatusDot>
                    </Table.DataCell>
                    <Table.DataCell className="whitespace-nowrap">
                      {s.state === "STALLED"
                        ? `ingen side på ${formatDuration(s.lastPageAt, null)}`
                        : formatDuration(
                            s.startedAt,
                            s.completedAt ?? s.lastPageAt,
                          )}
                    </Table.DataCell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        </div>
        <Pager page={page} total={syncs.total} />
      </section>
      <Outlet />
    </div>
  );
}
