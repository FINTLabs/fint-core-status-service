import {
  Box,
  Button,
  CopyButton,
  HStack,
  Loader,
  Pagination,
  Search,
  Table,
} from "@navikt/ds-react";
import { CheckmarkCircleIcon, XMarkOctagonIcon } from "@navikt/aksel-icons";
import type { IEvent } from "~/types/Event";
import { OperationBadge } from "~/components/events/eventUtils";
import { FilterActionMenu } from "~/components/common/FilterActionMenu";
import { formatTimestampDetailed } from "~/utils/time";

interface HendelserTableProps {
  data: IEvent[];
  onRowClick: (event: IEvent) => void;
  loading: boolean;
  currentPage: number;
  onPageChange: (page: number) => void;
  itemsPerPage: number;
  searchFilter: string;
  onSearchFilterChange: (value: string) => void;
  uniqueStatus: ("ok" | "error")[];
  uniqueOperation: ("CREATE" | "UPDATE" | "DELETE" | "VALIDATE" | "UNKNOWN")[];
  uniqueOrg: string[];
  uniqueResource: string[];
  activeFilters: {
    status: "all" | "ok" | "error" | "none";
    operation:
      | "all"
      | "CREATE"
      | "UPDATE"
      | "DELETE"
      | "VALIDATE"
      | "UNKNOWN"
      | "none";
    org: string;
    resource: string;
  };
  onStatusFilterChange: (value: "all" | "ok" | "error") => void;
  onOperationFilterChange: (
    value: "all" | "CREATE" | "UPDATE" | "DELETE" | "VALIDATE" | "UNKNOWN",
  ) => void;
  onOrgFilterChange: (value: string) => void;
  onResourceFilterChange: (value: string) => void;
  onClearFilters: () => void;
}

export function EventsTable({
  data,
  onRowClick,
  loading,
  currentPage,
  onPageChange,
  itemsPerPage,
  searchFilter,
  onSearchFilterChange,
  uniqueStatus,
  uniqueOperation,
  uniqueOrg,
  uniqueResource,
  activeFilters,
  onStatusFilterChange,
  onOperationFilterChange,
  onOrgFilterChange,
  onResourceFilterChange,
  onClearFilters,
}: HendelserTableProps) {
  const totalPages = Math.ceil(data.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedData = data.slice(startIndex, endIndex);

  return (
    <Box>
      <Box
        padding="space-16"
        borderRadius="8"
        shadow="dialog"
        marginBlock={"space-16"}
      >
        <HStack gap="space-16" align="end" marginBlock="space-16">
          <Box style={{ flex: 1 }}>
            <Search
              label="Søk hendelser ID"
              value={searchFilter}
              onChange={onSearchFilterChange}
              placeholder="Søk hendelser ID..."
              variant="secondary"
              size="small"
            />
          </Box>
          <Button size="small" variant="tertiary" onClick={onClearFilters}>
            Nullstill filtre
          </Button>
        </HStack>
        {loading && <Loader size="small" />}
        <Table size="small" zebraStripes={true}>
          <Table.Header>
            <Table.Row>
              <Table.HeaderCell>
                <HStack gap="space-4" align="center">
                  <span>Status</span>
                  <FilterActionMenu
                    title="Status"
                    options={uniqueStatus.map((status) => ({
                      value: status,
                      label: status === "ok" ? "OK" : "Feil",
                    }))}
                    selectedValue={
                      activeFilters.status === "all" ||
                      activeFilters.status === "none"
                        ? undefined
                        : activeFilters.status
                    }
                    onSelect={(value) =>
                      onStatusFilterChange(value as "all" | "ok" | "error")
                    }
                    onClear={() => onStatusFilterChange("all")}
                  />
                </HStack>
              </Table.HeaderCell>
              <Table.HeaderCell>Hendelse ID</Table.HeaderCell>
              <Table.HeaderCell>
                <HStack gap="space-4" align="center">
                  <span>Operasjon</span>
                  <FilterActionMenu
                    title="Operasjon"
                    options={uniqueOperation.map((operation) => ({
                      value: operation,
                      label: operation,
                    }))}
                    selectedValue={
                      activeFilters.operation === "all" ||
                      activeFilters.operation === "none"
                        ? undefined
                        : activeFilters.operation
                    }
                    onSelect={(value) =>
                      onOperationFilterChange(
                        value as
                          | "all"
                          | "CREATE"
                          | "UPDATE"
                          | "DELETE"
                          | "VALIDATE"
                          | "UNKNOWN",
                      )
                    }
                    onClear={() => onOperationFilterChange("all")}
                  />
                </HStack>
              </Table.HeaderCell>
              <Table.HeaderCell>
                <HStack gap="space-4" align="center">
                  <span>Organisasjon</span>
                  <FilterActionMenu
                    title="Organisasjon"
                    options={uniqueOrg.map((org) => ({
                      value: org,
                      label: org,
                    }))}
                    selectedValue={activeFilters.org || undefined}
                    onSelect={onOrgFilterChange}
                    onClear={() => onOrgFilterChange("")}
                  />
                </HStack>
              </Table.HeaderCell>
              <Table.HeaderCell>
                <HStack gap="space-4" align="center">
                  <span>Ressurser</span>
                  <FilterActionMenu
                    title="Ressurser"
                    options={uniqueResource.map((resource) => ({
                      value: resource,
                      label: resource,
                    }))}
                    selectedValue={activeFilters.resource || undefined}
                    onSelect={onResourceFilterChange}
                    onClear={() => onResourceFilterChange("")}
                  />
                </HStack>
              </Table.HeaderCell>
              <Table.HeaderCell>Request Dato</Table.HeaderCell>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {paginatedData.map((event, index) => (
              <Table.Row
                key={index}
                onRowClick={() => onRowClick(event)}
                shadeOnHover={true}
                data-cy="event-row"
              >
                <Table.DataCell>
                  {!event.hasError ? (
                    <CheckmarkCircleIcon
                      title="OK"
                      fontSize="1.5rem"
                      color="var(--ax-bg-success-strong)"
                    />
                  ) : (
                    <XMarkOctagonIcon
                      title="Error"
                      fontSize="1.25rem"
                      color="var(--ax-bg-danger-strong)"
                    />
                  )}
                </Table.DataCell>
                <Table.DataCell>
                  <HStack align="center" gap="space-8">
                    <CopyButton copyText={event.corrId} size={"small"} />
                    {event.corrId
                      ? `${event.corrId.substring(0, 5)}...${event.corrId.substring(event.corrId.length - 5)}`
                      : "N/A"}
                  </HStack>
                </Table.DataCell>
                <Table.DataCell>
                  {event.requestEvent?.operationType && (
                    <OperationBadge
                      operation={event.requestEvent?.operationType}
                    />
                  )}
                </Table.DataCell>
                <Table.DataCell>
                  <span className="text-ax-neutral-800">
                    {event.orgId || "N/A"}
                  </span>
                </Table.DataCell>
                <Table.DataCell>
                  <span className="text-ax-neutral-800">
                    {event.requestEvent?.resourceName || "N/A"}
                  </span>
                </Table.DataCell>
                <Table.DataCell>
                  {formatTimestampDetailed(event.requestEvent?.created)}
                </Table.DataCell>
              </Table.Row>
            ))}
          </Table.Body>
        </Table>
      </Box>
      {totalPages > 1 && (
        <Box
          paddingBlock="space-16"
          className="flex justify-center"
          data-cy="pagination"
        >
          <Pagination
            page={currentPage}
            onPageChange={onPageChange}
            count={totalPages}
            size="small"
            boundaryCount={1}
            siblingCount={1}
            data-cy="pagination"
          />
        </Box>
      )}
    </Box>
  );
}
