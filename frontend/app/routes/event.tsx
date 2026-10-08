import { BodyShort, Heading, Modal } from "@navikt/ds-react";
import { Link, useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type { EventDetail } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import { formatDateTime, resourcePath } from "~/lib/format";
import { Facts } from "~/components/Facts";
import { Mono } from "~/components/Mono";
import { StatusDot } from "~/components/StatusDot";
import { eventStatus } from "./events";
import type { Route } from "./+types/event";

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  return {
    env,
    detail: await statusApi.get<EventDetail>(
      request,
      env.key,
      `/events/${params.corrId}`,
    ),
  };
}

export default function EventModal({ loaderData }: Route.ComponentProps) {
  const { env, detail } = loaderData;
  const { event, candidates, adapterId } = detail;
  const navigate = useNavigate();
  const location = useLocation();
  const [tone, label] = eventStatus(event);
  const unanswered = event.status === "PENDING" || event.status === "EXPIRED";
  const close = () =>
    navigate(
      { pathname: `/${env.slug}/events`, search: location.search },
      { preventScrollReset: true },
    );

  return (
    <Modal
      open
      onClose={close}
      header={{ heading: "Event", closeButton: true }}
      width="medium"
    >
      <Modal.Body className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <BodyShort className="break-all">
            <Mono>{event.corrId}</Mono>
          </BodyShort>
          <StatusDot tone={tone}>{label}</StatusDot>
        </div>
        <Facts
          items={[
            ["Org", event.orgId],
            [
              "Ressurs",
              resourcePath(
                event.domainName,
                event.packageName,
                event.resourceName,
              ),
            ],
            ["Operasjon", event.operationType ?? "–"],
            ["Opprettet", formatDateTime(event.createdAt ?? event.receivedAt)],
            ["Frist", formatDateTime(event.expiresAt)],
            ["Behandlet", formatDateTime(event.handledAt)],
            ["Svart av", event.answeredBy ?? "ingen"],
            ["Adapter-id", adapterId ?? "–"],
            ["Grunn", event.reason ?? "–"],
          ]}
        />
        {unanswered ? (
          <section className="flex flex-col gap-2">
            <Heading size="xsmall" level="3">
              Hvem kunne svart
            </Heading>
            {candidates.length === 0 ? (
              <BodyShort
                size="small"
                className="rounded-md bg-[#FFE3E3] px-3 py-2 text-[#A30000]"
              >
                Ingen adapter har kontrakt for denne ressursen på denne org-en.
              </BodyShort>
            ) : (
              <>
                <BodyShort
                  size="small"
                  className="text-[var(--ax-text-neutral-subtle)]"
                >
                  Kontrakter for samme org som har denne ressursen i
                  kapabilitetene sine.
                </BodyShort>
                {candidates.map((c) => (
                  <div
                    key={c.contractId}
                    className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-[var(--ax-border-neutral-subtle)] px-3 py-2"
                  >
                    <Link to={`/${env.slug}/kontrakter/${c.contractId}`}>
                      <Mono>{c.username}</Mono>
                    </Link>
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
                      {c.muted
                        ? "dempet"
                        : c.heartbeat === "STOPPED"
                          ? "heartbeat stoppet"
                          : c.heartbeat === "NEVER"
                            ? "aldri heartbeat"
                            : "heartbeat ok"}
                    </StatusDot>
                  </div>
                ))}
              </>
            )}
          </section>
        ) : null}
      </Modal.Body>
    </Modal>
  );
}
