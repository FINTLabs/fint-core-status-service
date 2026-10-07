import {
  Box,
  Button,
  HStack,
  Pagination,
  ProgressBar,
  Search,
  Table,
  Tag,
} from "@navikt/ds-react";
import { ArrowCirclepathIcon, CheckmarkCircleIcon } from "@navikt/aksel-icons";
import type { ISyncData } from "~/types";
import { useMemo, useState } from "react";
import { formatTimestampDetailed } from "~/utils/time";
import { FilterActionMenu } from "~/components/common/FilterActionMenu";

interface SyncTableProps {
  data: ISyncData[];
  currentPage: number;
  onPageChange: (page: number) => void;
  itemsPerPage: number;
  onRowClick: (sync: ISyncData) => void;
  activeFilters: {
    status: "all" | "finished" | "ongoing" | "none";
    syncType: "all" | "FULL" | "DELTA" | "none";
    org: string;
    domain: string;
    package: string;
    resource: string;
  };
  uniqueOrg: string[];
  uniqueDomain: string[];
  uniquePackage: string[];
  uniqueResource: string[];
  onStatusFilterChange: (value: "all" | "finished" | "ongoing") => void;
  onSyncTypeFilterChange: (value: "all" | "FULL" | "DELTA") => void;
  onOrgFilterChange: (value: string) => void;
  onDomainFilterChange: (value: string) => void;
  onPackageFilterChange: (value: string) => void;
  onResourceFilterChange: (value: string) => void;
  adapterIdFilter: string;
  onAdapterIdFilterChange: (value: string) => void;
  onClearFilters: () => void;
}

export function SyncTable({
  data,
  currentPage,
  onPageChange,
  itemsPerPage,
  onRowClick,
  activeFilters,
  uniqueOrg,
  uniqueDomain,
  uniquePackage,
  uniqueResource,
  onStatusFilterChange,
  onSyncTypeFilterChange,
  onOrgFilterChange,
  onDomainFilterChange,
  onPackageFilterChange,
  onResourceFilterChange,
  adapterIdFilter,
  onAdapterIdFilterChange,
  onClearFilters,
}: SyncTableProps) {
  const [sort, setSort] = useState<
    { orderBy: string; direction: "ascending" | "descending" } | undefined
  >({ orderBy: "date", direction: "descending" });

  const sortedData = useMemo(() => {
    if (!sort) return data;

    return [...data].sort((a, b) => {
      if (sort.orderBy === "date") {
        const comparison = a.lastPageTime - b.lastPageTime;
        return sort.direction === "ascending" ? comparison : -comparison;
      }
      return 0;
    });
  }, [data, sort]);

  const totalPages = Math.ceil(sortedData.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedData = sortedData.slice(startIndex, endIndex);

  const handleSort = (sortKey: string) => {
    setSort((currentSort) => {
      if (!currentSort || currentSort.orderBy !== sortKey) {
        return { orderBy: sortKey, direction: "descending" };
      }
      if (currentSort.direction === "descending") {
        return { orderBy: sortKey, direction: "ascending" };
      }
      return undefined;
    });
  };

  const calculateProgress = (sync: ISyncData) => {
    return Math.round((sync.entitiesAquired / sync.totalEntities) * 100);
  };

  return (
    <Box
      padding="space-16"
      borderRadius="8"
      shadow="dialog"
      marginBlock={"space-16"}
    >
      <HStack gap="space-16" align="end" marginBlock="space-16">
        <Box style={{ flex: 1 }}>
          <Search
            label="Adapter ID"
            size="small"
            value={adapterIdFilter}
            onChange={onAdapterIdFilterChange}
            placeholder="Søk adapter ID..."
            variant="secondary"
          />
        </Box>
        <Button size="small" variant="tertiary" onClick={onClearFilters}>
          Nullstill filtre
        </Button>
      </HStack>
      <Table
        sort={sort}
        onSortChange={handleSort}
        size="small"
        zebraStripes={true}
      >
        <Table.Header>
          <Table.Row>
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Status</span>
                <FilterActionMenu
                  title="Status"
                  options={[
                    { value: "finished", label: "Fullfort" },
                    { value: "ongoing", label: "Pagar" },
                  ]}
                  selectedValue={
                    activeFilters.status === "all" ||
                    activeFilters.status === "none"
                      ? undefined
                      : activeFilters.status
                  }
                  onSelect={(value) =>
                    onStatusFilterChange(
                      value as "all" | "finished" | "ongoing",
                    )
                  }
                  onClear={() => onStatusFilterChange("all")}
                />
              </HStack>
            </Table.HeaderCell>
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Organisasjon</span>
                <FilterActionMenu
                  title="Organisasjon"
                  options={uniqueOrg.map((org) => ({ value: org, label: org }))}
                  selectedValue={activeFilters.org || undefined}
                  onSelect={onOrgFilterChange}
                  onClear={() => onOrgFilterChange("")}
                />
              </HStack>
            </Table.HeaderCell>
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Domene</span>
                <FilterActionMenu
                  title="Domene"
                  options={uniqueDomain.map((domain) => ({
                    value: domain,
                    label: domain,
                  }))}
                  selectedValue={activeFilters.domain || undefined}
                  onSelect={onDomainFilterChange}
                  onClear={() => onDomainFilterChange("")}
                />
              </HStack>
            </Table.HeaderCell>
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Pakke</span>
                <FilterActionMenu
                  title="Pakke"
                  options={uniquePackage.map((pkg) => ({
                    value: pkg,
                    label: pkg,
                  }))}
                  selectedValue={activeFilters.package || undefined}
                  onSelect={onPackageFilterChange}
                  onClear={() => onPackageFilterChange("")}
                />
              </HStack>
            </Table.HeaderCell>
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Ressurs</span>
                <FilterActionMenu
                  title="Ressurs"
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
            <Table.HeaderCell>
              <HStack gap="space-4" align="center">
                <span>Type</span>
                <FilterActionMenu
                  title="Type"
                  options={[
                    { value: "FULL", label: "FULL" },
                    { value: "DELTA", label: "DELTA" },
                  ]}
                  selectedValue={
                    activeFilters.syncType === "all" ||
                    activeFilters.syncType === "none"
                      ? undefined
                      : activeFilters.syncType
                  }
                  onSelect={(value) =>
                    onSyncTypeFilterChange(value as "all" | "FULL" | "DELTA")
                  }
                  onClear={() => onSyncTypeFilterChange("all")}
                />
              </HStack>
            </Table.HeaderCell>
            <Table.HeaderCell>Entiteter</Table.HeaderCell>
            <Table.HeaderCell>Sider</Table.HeaderCell>
            <Table.HeaderCell>Fremdrift</Table.HeaderCell>
            <Table.ColumnHeader sortKey="date" sortable>
              Sist oppdatert
            </Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {paginatedData.map((sync, index) => (
            <Table.Row
              key={index}
              data-cy="sync-row"
              onRowClick={() => onRowClick(sync)}
              shadeOnHover={true}
            >
              <Table.DataCell>
                {sync.finished ? (
                  <CheckmarkCircleIcon
                    color="var(--ax-bg-success-strong)"
                    title="Fullført"
                    fontSize="1.25rem"
                  />
                ) : (
                  <ArrowCirclepathIcon
                    color="var(--ax-bg-warning-strong)"
                    title="Pågår"
                    fontSize="1.25rem"
                  />
                )}
              </Table.DataCell>
              <Table.DataCell>{sync.orgId}</Table.DataCell>
              <Table.DataCell>{sync.domain}</Table.DataCell>
              <Table.DataCell>{sync.package}</Table.DataCell>
              <Table.DataCell>{sync.resource}</Table.DataCell>
              <Table.DataCell>
                <Tag
                  variant="outline"
                  size="xsmall"
                  data-color={sync.syncType === "FULL" ? "info" : "meta-purple"}
                >
                  {sync.syncType}
                </Tag>
              </Table.DataCell>
              <Table.DataCell>
                {sync.entitiesAquired}/{sync.totalEntities}
              </Table.DataCell>
              <Table.DataCell>
                {sync.pagesAcquired}/{sync.totalPages}
              </Table.DataCell>
              <Table.DataCell align="right">
                <Box paddingBlock="space-0">
                  <ProgressBar
                    data-color={sync.finished ? "success" : "warning"}
                    value={calculateProgress(sync)}
                    size="small"
                    aria-label={`Fremdrift: ${calculateProgress(sync)}%`}
                  />
                  <Box>{calculateProgress(sync)}%</Box>
                </Box>
              </Table.DataCell>
              <Table.DataCell>
                {formatTimestampDetailed(sync.lastPageTime)}
              </Table.DataCell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table>
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
          />
        </Box>
      )}
    </Box>
  );
}
