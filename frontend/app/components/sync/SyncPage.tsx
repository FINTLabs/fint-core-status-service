import { useCallback, useEffect, useMemo, useState } from "react";
import { Box } from "@navikt/ds-react";
import { SyncFilter } from "./SyncFilter";
import { SyncTable } from "./SyncTable";
import { SyncModal } from "./SyncModal";
import type { ISyncData } from "~/types";
import type { SyncFiltersState } from "~/types/Sync";
import { useSearchParams } from "react-router";

interface SyncPageProps {
  initialData: ISyncData[];
  env: string;
  dateRange: {
    from: Date | undefined;
    to: Date | undefined;
  };
  onDateRangeChange: (value: {
    from: Date | undefined;
    to: Date | undefined;
  }) => void;
}

export function SyncPage({
  initialData,
  dateRange,
  onDateRangeChange,
}: SyncPageProps) {
  const getStatusFilterValue = (filter: {
    finished: boolean;
    ongoing: boolean;
  }): "all" | "finished" | "ongoing" | "none" => {
    if (filter.finished && filter.ongoing) {
      return "all";
    }
    if (filter.finished) {
      return "finished";
    }
    if (filter.ongoing) {
      return "ongoing";
    }

    return "none";
  };

  const getSyncTypeFilterValue = (filter: {
    full: boolean;
    delta: boolean;
  }): "all" | "FULL" | "DELTA" | "none" => {
    if (filter.full && filter.delta) {
      return "all";
    }
    if (filter.full) {
      return "FULL";
    }
    if (filter.delta) {
      return "DELTA";
    }

    return "none";
  };

  const getDefaultFilters = (baseDateRange: {
    from: Date | undefined;
    to: Date | undefined;
  }): SyncFiltersState => ({
    syncTypeFilter: { full: true, delta: true },
    statusFilter: { finished: true, ongoing: true },
    orgFilter: "",
    domainFilter: "",
    packageFilter: "",
    resourceFilter: "",
    adapterIdFilter: "",
    dateRange: baseDateRange,
  });

  const getFiltersFromSearchParams = (
    params: URLSearchParams,
    fallbackDateRange: {
      from: Date | undefined;
      to: Date | undefined;
    },
  ): SyncFiltersState => {
    const getParam = (...keys: string[]) => {
      for (const key of keys) {
        const value = params.get(key);
        if (value !== null) {
          return value;
        }
      }
      return null;
    };

    const syncTypeParam = getParam("syncType", "syncFilter", "syncTypeFilter");
    const statusParam = getParam("status", "statusFilter");

    const fromParam = getParam("from");
    const toParam = getParam("to");
    const fromTimestamp =
      fromParam && !Number.isNaN(Number(fromParam))
        ? Number(fromParam)
        : undefined;
    const toTimestamp =
      toParam && !Number.isNaN(Number(toParam)) ? Number(toParam) : undefined;

    return {
      syncTypeFilter:
        syncTypeParam === "FULL"
          ? { full: true, delta: false }
          : syncTypeParam === "DELTA"
            ? { full: false, delta: true }
            : { full: true, delta: true },
      statusFilter:
        statusParam === "finished"
          ? { finished: true, ongoing: false }
          : statusParam === "ongoing"
            ? { finished: false, ongoing: true }
            : { finished: true, ongoing: true },
      orgFilter: getParam("org", "orgFilter") ?? "",
      domainFilter: getParam("domain", "domainFilter") ?? "",
      packageFilter: getParam("package", "packageFilter") ?? "",
      resourceFilter: getParam("resource", "resourceFilter") ?? "",
      adapterIdFilter: getParam("adapterId", "adapterIdFilter") ?? "",
      dateRange: {
        from:
          typeof fromTimestamp === "number"
            ? new Date(fromTimestamp)
            : fallbackDateRange.from,
        to:
          typeof toTimestamp === "number"
            ? new Date(toTimestamp)
            : fallbackDateRange.to,
      },
    };
  };

  const matchesFilters = (
    sync: ISyncData,
    filters: SyncFiltersState,
    ignoredKey?:
      | "orgFilter"
      | "domainFilter"
      | "packageFilter"
      | "resourceFilter"
      | "adapterIdFilter",
  ) => {
    if (!filters.syncTypeFilter.full && sync.syncType === "FULL") {
      return false;
    }
    if (!filters.syncTypeFilter.delta && sync.syncType === "DELTA") {
      return false;
    }

    if (!filters.statusFilter.finished && sync.finished) {
      return false;
    }
    if (!filters.statusFilter.ongoing && !sync.finished) {
      return false;
    }

    if (
      ignoredKey !== "orgFilter" &&
      filters.orgFilter &&
      sync.orgId.toLowerCase() !== filters.orgFilter.toLowerCase()
    ) {
      return false;
    }

    if (
      ignoredKey !== "domainFilter" &&
      filters.domainFilter &&
      sync.domain.toLowerCase() !== filters.domainFilter.toLowerCase()
    ) {
      return false;
    }

    if (
      ignoredKey !== "packageFilter" &&
      filters.packageFilter &&
      sync.package.toLowerCase() !== filters.packageFilter.toLowerCase()
    ) {
      return false;
    }

    if (
      ignoredKey !== "resourceFilter" &&
      filters.resourceFilter &&
      sync.resource.toLowerCase() !== filters.resourceFilter.toLowerCase()
    ) {
      return false;
    }

    if (
      ignoredKey !== "adapterIdFilter" &&
      filters.adapterIdFilter &&
      sync.adapterId.toLowerCase() !== filters.adapterIdFilter.toLowerCase()
    ) {
      return false;
    }

    if (filters.dateRange.from || filters.dateRange.to) {
      const syncDate = new Date(sync.lastPageTime);
      if (filters.dateRange.from && filters.dateRange.to) {
        const fromDate = new Date(filters.dateRange.from);
        const toDate = new Date(filters.dateRange.to);
        if (syncDate < fromDate || syncDate > toDate) {
          return false;
        }
      } else if (filters.dateRange.from) {
        const fromDate = new Date(filters.dateRange.from);
        if (syncDate < fromDate) {
          return false;
        }
      } else if (filters.dateRange.to) {
        const toDate = new Date(filters.dateRange.to);
        if (syncDate > toDate) {
          return false;
        }
      }
    }

    return true;
  };

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSync, setSelectedSync] = useState<ISyncData | null>(null);

  const [searchParams, setSearchParams] = useSearchParams();

  const [appliedFilters, setAppliedFilters] = useState<SyncFiltersState>(() =>
    getFiltersFromSearchParams(searchParams, dateRange),
  );

  useEffect(() => {
    setAppliedFilters((prev) => ({
      ...prev,
      dateRange,
    }));
  }, [dateRange]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const updateSearchParams = (updates: Record<string, string | undefined>) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);

      for (const [key, value] of Object.entries(updates)) {
        if (!value || value.trim() === "" || value === "all") {
          next.delete(key);
        } else {
          next.set(key, value);
        }
      }

      return next;
    });
  };

  const handleStatusFilterChange = (value: "all" | "finished" | "ongoing") => {
    setAppliedFilters((prev) => ({
      ...prev,
      statusFilter:
        value === "all"
          ? { finished: true, ongoing: true }
          : {
              finished: value === "finished",
              ongoing: value === "ongoing",
            },
    }));

    updateSearchParams({
      statusFilter: value === "all" ? undefined : value,
    });
    setCurrentPage(1);
  };

  const handleSyncTypeFilterChange = (value: "all" | "FULL" | "DELTA") => {
    setAppliedFilters((prev) => ({
      ...prev,
      syncTypeFilter:
        value === "all"
          ? { full: true, delta: true }
          : {
              full: value === "FULL",
              delta: value === "DELTA",
            },
    }));
    updateSearchParams({
      syncFilter: value === "all" ? undefined : value,
    });
    setCurrentPage(1);
  };

  const handleHeaderTextFilterChange = (
    key: "orgFilter" | "domainFilter" | "packageFilter" | "resourceFilter",
    value: string,
  ) => {
    setAppliedFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
    updateSearchParams({
      [key]: value,
    });
    setCurrentPage(1);
  };

  const handleAdapterIdFilterChange = (value: string) => {
    setAppliedFilters((prev) => ({
      ...prev,
      adapterIdFilter: value,
    }));
    setCurrentPage(1);
  };

  const handleApplyFilters = (value: SyncFiltersState) => {
    setAppliedFilters(value);
    setCurrentPage(1);

    const currentFrom = dateRange.from?.getTime();
    const currentTo = dateRange.to?.getTime();
    const nextFrom = value.dateRange.from?.getTime();
    const nextTo = value.dateRange.to?.getTime();
    const hasDateChange = currentFrom !== nextFrom || currentTo !== nextTo;

    if (hasDateChange) {
      onDateRangeChange(value.dateRange);
    }
  };

  const handleClearFilters = () => {
    const clearedDateRange = { from: undefined, to: undefined };

    setAppliedFilters(getDefaultFilters(clearedDateRange));
    setCurrentPage(1);

    onDateRangeChange(clearedDateRange);
    updateSearchParams({
      statusFilter: undefined,
      syncFilter: undefined,
      syncTypeFilter: undefined,
      org: undefined,
      orgFilter: undefined,
      domain: undefined,
      domainFilter: undefined,
      package: undefined,
      packageFilter: undefined,
      resource: undefined,
      resourceFilter: undefined,
      adapterId: undefined,
      adapterIdFilter: undefined,
    });
  };

  const handleRowClick = (sync: ISyncData) => {
    setSelectedSync(sync);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedSync(null);
  };

  const filteredData = useMemo(
    () => initialData.filter((sync) => matchesFilters(sync, appliedFilters)),
    [initialData, appliedFilters],
  );

  const getUniqueFilteredValues = useCallback(
    (
      ignoredKey:
        | "orgFilter"
        | "domainFilter"
        | "packageFilter"
        | "resourceFilter"
        | "adapterIdFilter",
      selector: (sync: ISyncData) => string,
    ) => {
      return [
        ...new Set(
          initialData
            .filter((sync) => matchesFilters(sync, appliedFilters, ignoredKey))
            .map(selector),
        ),
      ].sort((a, b) => a.localeCompare(b));
    },
    [initialData, appliedFilters],
  );

  const uniqueOrg = useMemo(() => {
    return getUniqueFilteredValues("orgFilter", (item) => item.orgId);
  }, [getUniqueFilteredValues]);

  const tableUniqueDomain = useMemo(() => {
    return getUniqueFilteredValues("domainFilter", (item) => item.domain);
  }, [getUniqueFilteredValues]);

  const tableUniquePackage = useMemo(() => {
    return getUniqueFilteredValues("packageFilter", (item) => item.package);
  }, [getUniqueFilteredValues]);

  const tableUniqueResource = useMemo(() => {
    return getUniqueFilteredValues("resourceFilter", (item) => item.resource);
  }, [getUniqueFilteredValues]);

  return (
    <Box>
      <SyncFilter
        filters={appliedFilters}
        onApplyFilters={handleApplyFilters}
      />
      <SyncTable
        data={filteredData}
        currentPage={currentPage}
        onPageChange={handlePageChange}
        itemsPerPage={itemsPerPage}
        onRowClick={handleRowClick}
        activeFilters={{
          status: getStatusFilterValue(appliedFilters.statusFilter),
          syncType: getSyncTypeFilterValue(appliedFilters.syncTypeFilter),
          org: appliedFilters.orgFilter,
          domain: appliedFilters.domainFilter,
          package: appliedFilters.packageFilter,
          resource: appliedFilters.resourceFilter,
        }}
        uniqueOrg={uniqueOrg}
        uniqueDomain={tableUniqueDomain}
        uniquePackage={tableUniquePackage}
        uniqueResource={tableUniqueResource}
        onStatusFilterChange={handleStatusFilterChange}
        onSyncTypeFilterChange={handleSyncTypeFilterChange}
        onOrgFilterChange={(value) =>
          handleHeaderTextFilterChange("orgFilter", value)
        }
        onDomainFilterChange={(value) =>
          handleHeaderTextFilterChange("domainFilter", value)
        }
        onPackageFilterChange={(value) =>
          handleHeaderTextFilterChange("packageFilter", value)
        }
        onResourceFilterChange={(value) =>
          handleHeaderTextFilterChange("resourceFilter", value)
        }
        adapterIdFilter={appliedFilters.adapterIdFilter}
        onAdapterIdFilterChange={handleAdapterIdFilterChange}
        onClearFilters={handleClearFilters}
      />
      {selectedSync && (
        <SyncModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          syncData={selectedSync}
        />
      )}
    </Box>
  );
}
