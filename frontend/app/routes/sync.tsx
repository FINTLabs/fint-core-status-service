import { BodyShort, Heading, Modal, Table } from "@navikt/ds-react";
import { useLocation, useNavigate } from "react-router";
import { statusApi } from "~/api/client.server";
import type { SyncDetail } from "~/api/types";
import { requireEnvironment } from "~/lib/env.server";
import {
  formatDateTime,
  formatDuration,
  formatNumber,
  resourcePath,
} from "~/lib/format";
import { Facts } from "~/components/Facts";
import { Mono } from "~/components/Mono";
import { StatusDot } from "~/components/StatusDot";
import { SYNC_STATE, SYNC_TYPE } from "./syncs";
import type { Route } from "./+types/sync";

export async function loader({ request, params }: Route.LoaderArgs) {
  const env = requireEnvironment(params.env);
  return {
    env,
    detail: await statusApi.get<SyncDetail>(
      request,
      env.key,
      `/syncs/${params.corrId}`,
    ),
  };
}

export default function SyncModal({ loaderData }: Route.ComponentProps) {
  const { env, detail } = loaderData;
  const { sync, pages } = detail;
  const navigate = useNavigate();
  const location = useLocation();
  const [tone, label] = SYNC_STATE[sync.state];
  const close = () =>
    navigate(
      { pathname: `/${env.slug}/syncer`, search: location.search },
      { preventScrollReset: true },
    );

  return (
    <Modal
      open
      onClose={close}
      header={{ heading: `${SYNC_TYPE[sync.type]}sync`, closeButton: true }}
      width="medium"
    >
      <Modal.Body className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <BodyShort className="break-all">
            <Mono>{sync.corrId}</Mono>
          </BodyShort>
          <StatusDot tone={tone}>{label}</StatusDot>
        </div>
        <Facts
          items={[
            ["Org", sync.orgId],
            [
              "Ressurs",
              resourcePath(
                sync.domainName,
                sync.packageName,
                sync.resourceName,
              ),
            ],
            ["Adapter-id", sync.adapterId],
            ["Startet", formatDateTime(sync.startedAt)],
            ["Siste side", formatDateTime(sync.lastPageAt)],
            ["Sider", `${sync.pagesReceived} av ${sync.totalPages}`],
            [
              "Entiteter",
              `${formatNumber(sync.entitiesReceived)} av ${formatNumber(sync.totalSize)}`,
            ],
            [
              "Varighet",
              formatDuration(
                sync.startedAt,
                sync.completedAt ?? sync.lastPageAt,
              ),
            ],
          ]}
        />
        <section className="flex flex-col gap-2">
          <Heading size="xsmall" level="3">
            Sider
          </Heading>
          <div className="max-h-80 overflow-y-auto">
            <Table size="small">
              <Table.Header>
                <Table.Row>
                  <Table.HeaderCell>Side</Table.HeaderCell>
                  <Table.HeaderCell>Størrelse</Table.HeaderCell>
                  <Table.HeaderCell>Mottatt</Table.HeaderCell>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {pages.map((p) => (
                  <Table.Row key={p.page}>
                    <Table.DataCell>{p.page}</Table.DataCell>
                    <Table.DataCell>{formatNumber(p.pageSize)}</Table.DataCell>
                    <Table.DataCell>
                      +{formatDuration(sync.startedAt, p.receivedAt)}
                    </Table.DataCell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table>
          </div>
        </section>
      </Modal.Body>
    </Modal>
  );
}
