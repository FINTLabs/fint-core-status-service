import type { ShouldRevalidateFunctionArgs } from "react-router";
import { Search, Select, Table } from "@navikt/ds-react";
import { Link, Outlet, useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type {
  ContractFilter,
  ContractPage,
  ContractSummary,
} from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import {
  PAGE_SIZE,
  pageFrom,
  useDebouncedValue,
  useFilters,
} from "~/lib/filters";
import { formatAgo } from "~/lib/format";
import { FilterBar, FilterRow } from "~/components/FilterBar";
import { PageHeading } from "~/components/PageHeading";
import { Pager } from "~/components/Pager";
import { Mono } from "~/components/Mono";
import { StatusDot } from "~/components/StatusDot";
import type { Route } from "./+types/contracts";

const FILTERS: [ContractFilter, string][] = [
  ["ALL", "Alle"],
  ["HEARTBEAT_STOPPED", "Heartbeat stoppet"],
  ["HEARTBEAT_NEVER", "Aldri heartbeat"],
  ["FULL_SYNC_OVERDUE", "Fullsync over intervall"],
  ["FULL_SYNC_NEVER", "Aldri sendt fullsync"],
  ["MUTED", "Dempet"],
];

const COLUMNS: [string, string][] = [
  ["org", "Org"],
  ["username", "Brukernavn"],
  ["heartbeat", "Heartbeat"],
  ["full-sync", "Fullsync"],
  ["capabilities", "Kapabiliteter"],
];

const toKebab = (value: string) => value.toLowerCase().replaceAll("_", "-");

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  const search = new URL(request.url).searchParams;
  const page = pageFrom(search);
  const contracts = await statusApi.get<ContractPage>(
    request,
    env.key,
    "/contracts",
    {
      q: search.get("sok"),
      filter: search.get("vis"),
      sort: search.get("sorter"),
      direction: search.get("retning"),
      page: page - 1,
      size: PAGE_SIZE,
    },
  );
  return { env, contracts, page };
}

export function shouldRevalidate({
  currentUrl,
  nextUrl,
  formMethod,
  defaultShouldRevalidate,
}: ShouldRevalidateFunctionArgs) {
  if (formMethod && formMethod !== "GET") return true;
  if (currentUrl.search === nextUrl.search) return false;
  return defaultShouldRevalidate;
}

function fullSyncText(contract: ContractSummary): string {
  const parts = [
    contract.fullSyncOverdue > 0 &&
      `${contract.fullSyncOverdue} over intervall`,
    contract.fullSyncNever > 0 && `${contract.fullSyncNever} aldri sendt`,
  ].filter(Boolean);
  return parts.join(", ") || "ok";
}

export default function ContractsPage({ loaderData }: Route.ComponentProps) {
  const { env, contracts, page } = loaderData;
  const { get, set } = useFilters();
  const navigate = useNavigate();
  const location = useLocation();
  const [query, setQuery] = useDebouncedValue(get("sok"), (value) =>
    set({ sok: value }),
  );
  const sort = get("sorter");
  const direction = get("retning") === "desc" ? "descending" : "ascending";

  const onSort = (key: string) => {
    const nextDirection =
      sort === key && direction === "ascending" ? "desc" : "asc";
    set({ sorter: key, retning: nextDirection });
  };

  const open = (id: number) =>
    navigate(
      { pathname: String(id), search: location.search },
      { preventScrollReset: true },
    );

  return (
    <div className="flex flex-col gap-3">
      <PageHeading
        title="Kontrakter"
        detail={`${contracts.total} kontrakter${contracts.total !== contracts.counts.ALL ? ` av ${contracts.counts.ALL}` : ""}${sort ? "" : " · problemer øverst"}`}
      />
      <FilterBar>
        <FilterRow>
          <div className="w-80 max-w-full">
            <Search
              label="Søk"
              hideLabel={false}
              size="small"
              variant="simple"
              placeholder="org, adapter eller brukernavn"
              value={query}
              onChange={setQuery}
              onClear={() => setQuery("")}
            />
          </div>
          <Select
            size="small"
            label="Vis"
            value={get("vis").toUpperCase().replaceAll("-", "_") || "ALL"}
            onChange={(e) =>
              set({
                vis: e.target.value === "ALL" ? null : toKebab(e.target.value),
              })
            }
          >
            {FILTERS.map(([key, label]) => (
              <option key={key} value={key}>
                {label} ({contracts.counts[key] ?? 0})
              </option>
            ))}
          </Select>
        </FilterRow>
      </FilterBar>
      <section className="overflow-hidden rounded-lg border border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)]">
        <div className="overflow-x-auto">
          <Table
            size="small"
            sort={sort ? { orderBy: sort, direction } : undefined}
            onSortChange={onSort}
          >
            <Table.Header>
              <Table.Row>
                {COLUMNS.map(([key, label]) => (
                  <Table.ColumnHeader
                    key={key}
                    sortKey={key}
                    sortable
                    align={key === "capabilities" ? "right" : undefined}
                  >
                    {label}
                  </Table.ColumnHeader>
                ))}
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {contracts.items.map((c) => (
                <Table.Row
                  key={c.id}
                  className="cursor-pointer"
                  onClick={() => open(c.id)}
                >
                  <Table.DataCell style={{ whiteSpace: "nowrap" }}>
                    <Link
                      to={{ pathname: String(c.id), search: location.search }}
                      preventScrollReset
                      onClick={(e) => e.stopPropagation()}
                      className="font-semibold"
                    >
                      {c.orgId}
                    </Link>
                    {c.orgId !== c.mainOrgId ? (
                      <span className="ml-1.5 text-xs text-[var(--ax-text-neutral-subtle)]">
                        under {c.mainOrgId}
                      </span>
                    ) : null}
                    {c.muted ? (
                      <span className="ml-2 rounded-full bg-[var(--ax-bg-neutral-soft)] px-2 py-0.5 text-xs font-semibold">
                        dempet
                      </span>
                    ) : null}
                  </Table.DataCell>
                  <Table.DataCell>
                    <Mono>{c.username}</Mono>
                  </Table.DataCell>
                  <Table.DataCell>
                    <StatusDot
                      tone={
                        c.muted
                          ? "muted"
                          : c.heartbeat === "STOPPED"
                            ? "bad"
                            : c.heartbeat === "NEVER"
                              ? "warn"
                              : "ok"
                      }
                    >
                      {c.heartbeat === "NEVER"
                        ? "aldri mottatt"
                        : `${c.heartbeat === "STOPPED" ? "stoppet " : ""}${formatAgo(c.lastHeartbeatAt)}`}
                    </StatusDot>
                  </Table.DataCell>
                  <Table.DataCell>
                    <StatusDot
                      tone={
                        c.muted
                          ? "muted"
                          : c.fullSyncOverdue || c.fullSyncNever
                            ? "bad"
                            : "ok"
                      }
                    >
                      {fullSyncText(c)}
                    </StatusDot>
                  </Table.DataCell>
                  <Table.DataCell align="right">
                    {c.capabilities}
                  </Table.DataCell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table>
        </div>
        <Pager page={page} total={contracts.total} />
      </section>
      <Outlet context={{ envSlug: env.slug }} />
    </div>
  );
}
