import {
  Box,
  Button,
  CopyButton,
  Detail,
  Heading,
  HGrid,
  HStack,
  Label,
  Link,
  Modal,
  Table,
  Tag,
  VStack,
} from "@navikt/ds-react";
import type { ISyncData } from "~/types";
import { formatTimestampDetailed } from "~/utils/time";

interface SyncModalProps {
  isOpen: boolean;
  onClose: () => void;
  syncData: ISyncData;
}

export function SyncModal({ isOpen, onClose, syncData }: SyncModalProps) {
  const calculateDuration = (pages: ISyncData["pages"]) => {
    if (pages.length === 0) return "N/A";
    const firstPageTime = pages[0].time;
    const lastPageTime = pages[pages.length - 1].time;
    const durationMs = lastPageTime - firstPageTime;
    const durationSeconds = Math.round(durationMs / 1000);
    if (durationSeconds < 60) {
      return `${durationSeconds} sekunder`;
    }
    const minutes = Math.floor(durationSeconds / 60);
    const seconds = durationSeconds % 60;
    return `${minutes}m ${seconds}s`;
  };

  const calculateProgress = () => {
    return Math.round(
      (syncData.entitiesAquired / syncData.totalEntities) * 100,
    );
  };

  return (
    <Modal
      open={isOpen}
      onClose={onClose}
      width="medium"
      data-cy="sync-modal"
      aria-label="Synkronisering Detaljer"
    >
      <Modal.Header>
        <VStack gap="space-8">
          <Heading level="2" size="medium">
            Synkronisering Detaljer
          </Heading>
          <VStack gap="space-4">
            <Box>
              <Label>Correlation ID</Label>
              <HStack align="center" gap="space-8">
                <Detail>{syncData.corrId}</Detail>
                <CopyButton copyText={syncData.corrId} size={"small"} />
              </HStack>
            </Box>
            <Box>
              <Label>Adapter ID</Label>
              <HStack align="center" gap="space-8">
                <Detail>
                  <Link href={`/adaptere/${syncData.orgId}/${syncData.domain}`}>
                    {syncData.adapterId}
                  </Link>
                </Detail>
                <CopyButton copyText={syncData.adapterId} size={"small"} />
              </HStack>
            </Box>
          </VStack>
        </VStack>
      </Modal.Header>
      <Modal.Body>
        <Heading level="3" size="small" spacing data-color={"brand-magenta"}>
          Oversikt
        </Heading>
        <HGrid gap="space-16" columns={2}>
          <Box>
            <Label>Organisasjon</Label>
            <Detail>{syncData.orgId}</Detail>
          </Box>

          <Box>
            <Label>Domene</Label>
            <Detail>{syncData.domain}</Detail>
          </Box>

          <Box>
            <Label>Pakke</Label>
            <Detail>{syncData.package}</Detail>
          </Box>

          <Box>
            <Label>Ressurs</Label>
            <Detail>{syncData.resource}</Detail>
          </Box>

          <Box>
            <Label>Type </Label>
            <Tag
              variant="outline"
              size="xsmall"
              data-color={syncData.syncType === "FULL" ? "info" : "meta-purple"}
            >
              {syncData.syncType}
            </Tag>
          </Box>

          <Box>
            <Label>Status </Label>
            <Tag
              variant="outline"
              size="xsmall"
              data-color={syncData.finished ? "success" : "warning"}
            >
              {syncData.finished ? "Fullført" : "Pågår"}
            </Tag>
          </Box>

          <Box>
            <Label>Fremdrift</Label>
            <Detail>
              {syncData.entitiesAquired} / {syncData.totalEntities} entiteter (
              {calculateProgress()}%)
            </Detail>
          </Box>

          <Box>
            <Label>Sider</Label>
            <Detail>
              {syncData.pagesAcquired} / {syncData.totalPages} sider
            </Detail>
          </Box>

          <Box>
            <Label>Varighet</Label>
            <Detail>{calculateDuration(syncData.pages)}</Detail>
          </Box>

          <Box>
            <Label>Sist oppdatert</Label>
            <Detail>{formatTimestampDetailed(syncData.lastPageTime)}</Detail>
          </Box>
        </HGrid>

        {/* Pages Table */}
        <Box marginBlock="space-16">
          <Heading level="3" size="small" spacing data-color={"brand-magenta"}>
            Sider ({syncData.pages.length})
          </Heading>

          <Table size="small" zebraStripes={true}>
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell>Side Nr.</Table.HeaderCell>
                <Table.HeaderCell>Størrelse</Table.HeaderCell>
                <Table.HeaderCell>Tidspunkt</Table.HeaderCell>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {syncData.pages.map((page, index) => (
                <Table.Row key={index}>
                  <Table.DataCell>{page.pageNumber}</Table.DataCell>
                  <Table.DataCell>{page.pageSize} entiteter</Table.DataCell>
                  <Table.DataCell>
                    {formatTimestampDetailed(page.time)}
                  </Table.DataCell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table>
        </Box>
      </Modal.Body>
      <Modal.Footer>
        <HStack gap="space-16" justify="end">
          <Button
            variant="tertiary"
            onClick={onClose}
            data-cy="sync-modal-close"
          >
            Lukk
          </Button>
        </HStack>
      </Modal.Footer>
    </Modal>
  );
}
