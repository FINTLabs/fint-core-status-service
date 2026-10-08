import {
  BodyShort,
  Button,
  DatePicker,
  Heading,
  Modal,
  Table,
  Tag,
  TextField,
  useDatepicker,
} from "@navikt/ds-react";
import { useFetcher, useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type { ContractDetail } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import { formatAgo, formatDateTime } from "~/lib/format";
import { Facts } from "~/components/Facts";
import { Mono } from "~/components/Mono";
import { StatusDot } from "~/components/StatusDot";
import type { Route } from "./+types/contract";

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  return {
    env,
    contract: await statusApi.get<ContractDetail>(
      request,
      env.key,
      `/contracts/${params.id}`,
    ),
  };
}

export async function action({ request, params }: Route.ActionArgs) {
  const env = requireEnvironment(params.env);
  const form = await request.formData();
  if (form.get("intent") === "unmute") {
    await statusApi.delete(request, env.key, `/contracts/${params.id}/mute`);
    return { ok: true };
  }
  const until = String(form.get("until") ?? "");
  await statusApi.put(request, env.key, `/contracts/${params.id}/mute`, {
    reason: String(form.get("reason") ?? "") || null,
    mutedUntil: until ? new Date(`${until}T23:59:59`).toISOString() : null,
  });
  return { ok: true };
}

function toIsoDate(day: Date): string {
  return `${day.getFullYear()}-${String(day.getMonth() + 1).padStart(2, "0")}-${String(day.getDate()).padStart(2, "0")}`;
}

function MuteUntilField() {
  const { datepickerProps, inputProps, selectedDay } = useDatepicker({
    fromDate: new Date(),
  });
  return (
    <>
      <DatePicker {...datepickerProps}>
        <DatePicker.Input
          {...inputProps}
          size="small"
          label="Demp til (valgfri)"
        />
      </DatePicker>
      <input
        type="hidden"
        name="until"
        value={selectedDay ? toIsoDate(selectedDay) : ""}
      />
    </>
  );
}

const HEARTBEAT_TEXT = {
  OK: "Heartbeat ok",
  STOPPED: "Heartbeat stoppet",
  NEVER: "Aldri mottatt heartbeat",
} as const;

export default function ContractModal({ loaderData }: Route.ComponentProps) {
  const { env, contract } = loaderData;
  const navigate = useNavigate();
  const location = useLocation();
  const fetcher = useFetcher();
  const close = () =>
    navigate(
      { pathname: `/${env.slug}/kontrakter`, search: location.search },
      { preventScrollReset: true },
    );
  const busy = fetcher.state !== "idle";

  return (
    <Modal
      open
      onClose={close}
      header={{ heading: "Kontrakt", closeButton: true }}
      width="medium"
    >
      <Modal.Body className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <BodyShort className="break-all">
            <Mono>
              {contract.username} · {contract.orgId}
            </Mono>
          </BodyShort>
          <StatusDot
            tone={
              contract.mute
                ? "muted"
                : contract.heartbeat === "STOPPED"
                  ? "bad"
                  : contract.heartbeat === "NEVER"
                    ? "warn"
                    : "ok"
            }
          >
            {HEARTBEAT_TEXT[contract.heartbeat]}
          </StatusDot>
        </div>
        <Facts
          items={[
            [
              "Org",
              contract.orgId === contract.mainOrgId
                ? contract.orgId
                : `${contract.orgId} (under ${contract.mainOrgId})`,
            ],
            ["Brukernavn", contract.username],
            ["Adapter-id", contract.adapterId],
            ["Heartbeat-intervall", `${contract.heartbeatIntervalMinutes} min`],
            ["Siste heartbeat", formatAgo(contract.lastHeartbeatAt)],
            [
              "Registrert første gang",
              formatDateTime(contract.firstRegisteredAt),
            ],
            ["Sist registrert", formatDateTime(contract.registeredAt)],
          ]}
        />

        <section className="flex flex-col gap-2 rounded-lg border border-[var(--ax-border-neutral-subtle)] p-3">
          <Heading size="xsmall" level="3">
            Varsler
          </Heading>
          {contract.mute ? (
            <fetcher.Form method="post" className="flex flex-col gap-2">
              <BodyShort size="small">
                Dempet av {contract.mute.mutedBy}{" "}
                {formatDateTime(contract.mute.mutedAt)}
                {contract.mute.reason ? `: ${contract.mute.reason}` : ""}
                {contract.mute.mutedUntil
                  ? `, til ${formatDateTime(contract.mute.mutedUntil)}`
                  : ""}
              </BodyShort>
              <input type="hidden" name="intent" value="unmute" />
              <Button
                size="small"
                variant="secondary"
                className="self-start"
                loading={busy}
              >
                Slå på varsler igjen
              </Button>
            </fetcher.Form>
          ) : (
            <fetcher.Form
              method="post"
              className="flex flex-wrap items-end gap-2"
            >
              <input type="hidden" name="intent" value="mute" />
              <TextField
                size="small"
                name="reason"
                label="Grunn (valgfri)"
                placeholder="f.eks. testadapter"
                className="min-w-56 flex-1"
              />
              <MuteUntilField />
              <Button size="small" variant="secondary" loading={busy}>
                Demp varsler
              </Button>
            </fetcher.Form>
          )}
          <BodyShort
            size="small"
            className="text-[var(--ax-text-neutral-subtle)]"
          >
            En dempet kontrakt vises fortsatt, men teller ikke som problem på
            oversikten eller i Grafana.
          </BodyShort>
        </section>

        <section className="flex flex-col gap-2">
          <Heading size="xsmall" level="3">
            Kapabiliteter
          </Heading>
          <Table size="small">
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell>Ressurs</Table.HeaderCell>
                <Table.HeaderCell>Intervall</Table.HeaderCell>
                <Table.HeaderCell>Siste fullsync</Table.HeaderCell>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {contract.capabilities.map((cap) => (
                <Table.Row
                  key={`${cap.domainName}/${cap.packageName}/${cap.resourceName}`}
                >
                  <Table.DataCell>
                    <Mono>
                      {cap.domainName}/{cap.packageName}/{cap.resourceName}
                    </Mono>
                  </Table.DataCell>
                  <Table.DataCell>{cap.fullSyncIntervalDays} d</Table.DataCell>
                  <Table.DataCell>
                    <StatusDot
                      tone={
                        cap.fullSync === "OVERDUE" || cap.fullSync === "NEVER"
                          ? "bad"
                          : cap.fullSync === "WAITING"
                            ? "muted"
                            : "ok"
                      }
                    >
                      {cap.lastFullSyncAt
                        ? formatAgo(cap.lastFullSyncAt)
                        : cap.fullSync === "WAITING"
                          ? "venter på første"
                          : "aldri"}
                    </StatusDot>
                  </Table.DataCell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table>
        </section>

        <section className="flex flex-col gap-2">
          <Heading size="xsmall" level="3">
            Svarer på events
          </Heading>
          {contract.eventCapabilities.length === 0 ? (
            <BodyShort
              size="small"
              className="text-[var(--ax-text-neutral-subtle)]"
            >
              Kontrakten sier ikke hvilke events adapteren svarer på.
            </BodyShort>
          ) : (
            <Table size="small">
              <Table.Header>
                <Table.Row>
                  <Table.HeaderCell>Ressurs</Table.HeaderCell>
                  <Table.HeaderCell>Operasjoner</Table.HeaderCell>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {contract.eventCapabilities.map((cap) => (
                  <Table.Row
                    key={`${cap.domainName}/${cap.packageName}/${cap.resourceName}`}
                  >
                    <Table.DataCell>
                      <Mono>
                        {cap.domainName}/{cap.packageName}/{cap.resourceName}
                      </Mono>
                    </Table.DataCell>
                    <Table.DataCell>
                      <span className="flex flex-wrap gap-1">
                        {cap.operations.map((op) => (
                          <Tag key={op} size="xsmall" variant="neutral">
                            {op}
                          </Tag>
                        ))}
                      </span>
                    </Table.DataCell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table>
          )}
        </section>
      </Modal.Body>
    </Modal>
  );
}
