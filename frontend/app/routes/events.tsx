import type { ShouldRevalidateFunctionArgs } from "react-router";
import { Button, Chips, Table } from "@navikt/ds-react";
import { Outlet, useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type {
  EventPage,
  EventSummary,
  ModelDomain,
  OrgNode,
} from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import { PAGE_SIZE, pageFrom, useFilters } from "~/lib/filters";
import { formatDateTime, minutesSince, resourcePath } from "~/lib/format";
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
import type { Route } from "./+types/events";

const STATUSES: [string, string][] = [
  ["", "Alle"],
  ["pending", "Venter"],
  ["expired", "Utløpt"],
  ["errors", "Feilet, avvist, konflikt"],
  ["answered", "Besvart"],
];

export const EVENT_STATUS: Record<EventSummary["status"], [Tone, string]> = {
  PENDING: ["muted", "Venter"],
  EXPIRED: ["bad", "Utløpt"],
  ANSWERED: ["ok", "Besvart"],
  FAILED: ["warn", "Feilet"],
  REJECTED: ["warn", "Avvist"],
  CONFLICTED: ["warn", "Konflikt"],
};

export function eventStatus(event: EventSummary): [Tone, string] {
  if (event.status !== "PENDING") return EVENT_STATUS[event.status];
  const minutes = minutesSince(event.createdAt ?? event.receivedAt);
  return [minutes >= 5 ? "warn" : "muted", `Venter · ${minutes} min`];
}

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  const search = new URL(request.url).searchParams;
  const page = pageFrom(search);
  const loadedAt = new Date().toISOString();
  const filters = {
    status: search.get("status"),
    ...sharedFilterParams(search),
  };
  const [events, orgs, model] = await Promise.all([
    statusApi.get<EventPage>(request, env.key, "/events", {
      ...filters,
      page: page - 1,
      size: PAGE_SIZE,
    }),
    statusApi.get<OrgNode[]>(request, env.key, "/orgs"),
    statusApi.get<ModelDomain[]>(request, env.key, "/model"),
  ]);
  const countUrl = `/${env.slug}/antall/events?${toQueryString({ ...filters, since: loadedAt })}`;
  return { env, events, orgs, model, page, countUrl };
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

export default function EventsPage({ loaderData }: Route.ComponentProps) {
  const { events, orgs, model, page, countUrl } = loaderData;
  const { get, set } = useFilters();
  const navigate = useNavigate();
  const location = useLocation();
  const status = get("status");

  return (
    <div className="flex flex-col gap-3">
      <PageHeading
        title="Events"
        detail={`${events.total} events siste døgn`}
      />
      <FilterBar>
        <FilterRow>
          <Chips size="small" aria-label="Status">
            {STATUSES.map(([key, label]) => (
              <Chips.Toggle
                key={key || "all"}
                selected={status === key}
                checkmark={false}
                onClick={() => set({ status: key })}
              >
                {label}
              </Chips.Toggle>
            ))}
          </Chips>
          <NewItemsBanner countUrl={countUrl} label="nye events" />
        </FilterRow>
        <FilterRow>
          <CorrIdField />
          <OrgSelect orgs={orgs} />
          <ResourceSelects model={model} />
          <Button
            size="small"
            variant="tertiary"
            onClick={() => navigate({ search: "" })}
          >
            Nullstill
          </Button>
        </FilterRow>
      </FilterBar>
      <section className="overflow-hidden rounded-lg border border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)]">
        <div className="overflow-x-auto">
          <Table size="small">
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell>Opprettet</Table.HeaderCell>
                <Table.HeaderCell>Status</Table.HeaderCell>
                <Table.HeaderCell>Org</Table.HeaderCell>
                <Table.HeaderCell>Ressurs</Table.HeaderCell>
                <Table.HeaderCell>Operasjon</Table.HeaderCell>
                <Table.HeaderCell>Svart av / kunne svart</Table.HeaderCell>
                <Table.HeaderCell>CorrId</Table.HeaderCell>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {events.items.map((e) => {
                const [tone, label] = eventStatus(e);
                const who =
                  e.answeredBy ??
                  (e.candidates.length
                    ? e.candidates.join(", ")
                    : e.status === "PENDING" || e.status === "EXPIRED"
                      ? "ingen med kontrakt"
                      : "–");
                return (
                  <Table.Row
                    key={e.corrId}
                    className="cursor-pointer"
                    onClick={() =>
                      navigate(
                        { pathname: e.corrId, search: location.search },
                        { preventScrollReset: true },
                      )
                    }
                  >
                    <Table.DataCell className="whitespace-nowrap">
                      {formatDateTime(e.createdAt ?? e.receivedAt)}
                    </Table.DataCell>
                    <Table.DataCell>
                      <StatusDot tone={tone}>{label}</StatusDot>
                    </Table.DataCell>
                    <Table.DataCell>{e.orgId}</Table.DataCell>
                    <Table.DataCell>
                      <Mono>
                        {resourcePath(
                          e.domainName,
                          e.packageName,
                          e.resourceName,
                        )}
                      </Mono>
                    </Table.DataCell>
                    <Table.DataCell>{e.operationType ?? "–"}</Table.DataCell>
                    <Table.DataCell>
                      <Mono>{who}</Mono>
                    </Table.DataCell>
                    <Table.DataCell>
                      <Mono>{e.corrId.slice(0, 8)}</Mono>
                    </Table.DataCell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        </div>
        <Pager page={page} total={events.total} />
      </section>
      <Outlet />
    </div>
  );
}
