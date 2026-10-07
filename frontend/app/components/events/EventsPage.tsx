import { useCallback, useEffect, useMemo, useState } from "react";
import { Box } from "@navikt/ds-react";
import { EventsFilter, type EventsFilters } from "./EventsFilter";
import { EventsModal } from "./EventsModal";

import { EventsTable } from "./EventsTable";
import type { IEvent } from "~/types/Event";

interface EventPageProps {
  initialData: IEvent[];
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

export function EventsPage({
  initialData,
  dateRange,
  onDateRangeChange,
}: EventPageProps) {
  const getStatusFilterValue = (filter: {
    ok: boolean;
    error: boolean;
  }): "all" | "ok" | "error" | "none" => {
    if (filter.ok && filter.error) {
      return "all";
    }
    if (filter.ok) {
      return "ok";
    }
    if (filter.error) {
      return "error";
    }

    return "none";
  };

  const getOperationFilterValue = (filter: {
    CREATE: boolean;
    UPDATE: boolean;
    DELETE: boolean;
    VALIDATE: boolean;
    UNKNOWN: boolean;
  }):
    | "all"
    | "CREATE"
    | "UPDATE"
    | "DELETE"
    | "VALIDATE"
    | "UNKNOWN"
    | "none" => {
    if (
      filter.CREATE &&
      filter.UPDATE &&
      filter.DELETE &&
      filter.VALIDATE &&
      filter.UNKNOWN
    ) {
      return "all";
    }
    if (filter.CREATE) {
      return "CREATE";
    }
    if (filter.UPDATE) {
      return "UPDATE";
    }
    if (filter.DELETE) {
      return "DELETE";
    }
    if (filter.VALIDATE) {
      return "VALIDATE";
    }
    if (filter.UNKNOWN) {
      return "UNKNOWN";
    }

    return "none";
  };

  const getDefaultFilters = (baseDateRange: {
    from: Date | undefined;
    to: Date | undefined;
  }): EventsFilters => ({
    searchFilter: "",
    dateRange: baseDateRange,
    operationFilter: {
      CREATE: true,
      UPDATE: true,
      DELETE: true,
      VALIDATE: true,
      UNKNOWN: true,
    },
    orgFilter: "",
    resourceFilter: "",
    statusFilter: { ok: true, error: true },
  });

  const matchesFilters = (
    event: IEvent,
    filters: EventsFilters,
    ignoredKey?:
      | "statusFilter"
      | "operationFilter"
      | "orgFilter"
      | "resourceFilter",
  ) => {
    if (!event.requestEvent) {
      return false;
    }

    if (filters.searchFilter) {
      const searchTerm = filters.searchFilter.toLowerCase();
      const matchesCorrId = event.corrId?.toLowerCase().includes(searchTerm);
      const matchesOrgId = event.orgId?.toLowerCase().includes(searchTerm);
      const matchesResource = event.requestEvent.resourceName
        ?.toLowerCase()
        .includes(searchTerm);
      const matchesTopic = event.topic?.toLowerCase().includes(searchTerm);

      if (
        !matchesCorrId &&
        !matchesOrgId &&
        !matchesResource &&
        !matchesTopic
      ) {
        return false;
      }
    }

    if (ignoredKey !== "operationFilter") {
      const operationType =
        event.requestEvent.operationType?.toUpperCase() ?? "UNKNOWN";
      if (
        !filters.operationFilter[
          operationType as keyof typeof filters.operationFilter
        ]
      ) {
        return false;
      }
    }

    if (
      ignoredKey !== "orgFilter" &&
      filters.orgFilter &&
      event.orgId.toLowerCase() !== filters.orgFilter.toLowerCase()
    ) {
      return false;
    }

    if (
      ignoredKey !== "resourceFilter" &&
      filters.resourceFilter &&
      event.requestEvent.resourceName.toLowerCase() !==
        filters.resourceFilter.toLowerCase()
    ) {
      return false;
    }

    if (ignoredKey !== "statusFilter") {
      if (!filters.statusFilter.ok && !event.hasError) {
        return false;
      }
      if (!filters.statusFilter.error && event.hasError) {
        return false;
      }
    }

    if (filters.dateRange.from || filters.dateRange.to) {
      const eventDate = new Date(event.requestEvent.created);
      if (filters.dateRange.from && filters.dateRange.to) {
        const fromDate = new Date(filters.dateRange.from);
        const toDate = new Date(filters.dateRange.to);
        if (eventDate < fromDate || eventDate > toDate) {
          return false;
        }
      } else if (filters.dateRange.from) {
        const fromDate = new Date(filters.dateRange.from);
        if (eventDate < fromDate) {
          return false;
        }
      } else if (filters.dateRange.to) {
        const toDate = new Date(filters.dateRange.to);
        if (eventDate > toDate) {
          return false;
        }
      }
    }

    return true;
  };

  const routeFromTimestamp = dateRange.from?.getTime();
  const routeToTimestamp = dateRange.to?.getTime();

  const [appliedFilters, setAppliedFilters] = useState<EventsFilters>(() =>
    getDefaultFilters(dateRange),
  );

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<IEvent | null>(null);

  useEffect(() => {
    setAppliedFilters((prev) => {
      const prevFromTimestamp = prev.dateRange.from?.getTime();
      const prevToTimestamp = prev.dateRange.to?.getTime();
      const hasDateRangeChange =
        prevFromTimestamp !== routeFromTimestamp ||
        prevToTimestamp !== routeToTimestamp;

      if (!hasDateRangeChange) {
        return prev;
      }

      return {
        ...prev,
        dateRange: {
          from: dateRange.from,
          to: dateRange.to,
        },
      };
    });
  }, [routeFromTimestamp, routeToTimestamp, dateRange.from, dateRange.to]);

  const handleApplyFilters = (value: EventsFilters) => {
    setAppliedFilters(value);
    setCurrentPage(1);

    const currentFrom = appliedFilters.dateRange.from?.getTime();
    const currentTo = appliedFilters.dateRange.to?.getTime();
    const nextFrom = value.dateRange.from?.getTime();
    const nextTo = value.dateRange.to?.getTime();
    const hasDateChange = currentFrom !== nextFrom || currentTo !== nextTo;

    if (hasDateChange) {
      onDateRangeChange(value.dateRange);
    }
  };

  const handleSearchFilterChange = (value: string) => {
    setAppliedFilters((prev) => ({
      ...prev,
      searchFilter: value,
    }));
    setCurrentPage(1);
  };

  const handleStatusFilterChange = (value: "all" | "ok" | "error") => {
    setAppliedFilters((prev) => ({
      ...prev,
      statusFilter:
        value === "all"
          ? { ok: true, error: true }
          : {
              ok: value === "ok",
              error: value === "error",
            },
    }));
    setCurrentPage(1);
  };

  const handleOperationFilterChange = (
    value: "all" | "CREATE" | "UPDATE" | "DELETE" | "VALIDATE" | "UNKNOWN",
  ) => {
    setAppliedFilters((prev) => ({
      ...prev,
      operationFilter:
        value === "all"
          ? {
              CREATE: true,
              UPDATE: true,
              DELETE: true,
              VALIDATE: true,
              UNKNOWN: true,
            }
          : {
              CREATE: value === "CREATE",
              UPDATE: value === "UPDATE",
              DELETE: value === "DELETE",
              VALIDATE: value === "VALIDATE",
              UNKNOWN: value === "UNKNOWN",
            },
    }));
    setCurrentPage(1);
  };

  const handleOrgFilterChange = (value: string) => {
    setAppliedFilters((prev) => ({
      ...prev,
      orgFilter: value,
    }));
    setCurrentPage(1);
  };

  const handleResourceFilterChange = (value: string) => {
    setAppliedFilters((prev) => ({
      ...prev,
      resourceFilter: value,
    }));
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    const clearedDateRange = { from: undefined, to: undefined };
    setAppliedFilters(getDefaultFilters(clearedDateRange));
    setCurrentPage(1);
    onDateRangeChange(clearedDateRange);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleRowClick = (event: IEvent) => {
    setSelectedEvent(event);
    setIsModalOpen(true);
    // fetchEventDetail(event.eventId);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedEvent(null);
  };

  // const [eventDetailData, setEventDetailData] = useState<IEventDetail | null>(null);
  // const [loadingDetail, setLoadingDetail] = useState(false);

  // const fetchEventDetail = async (eventId: string) => {
  //   try {
  //     setLoadingDetail(true);
  //     const response = await EventsApi.getEventDetail(eventId);
  //     setEventDetailData(response.data || null);
  //   } catch {
  //     setEventDetailData(null);
  //   } finally {
  //     setLoadingDetail(false);
  //   }
  // };

  const filteredData = useMemo(() => {
    const filtered = initialData.filter((event) =>
      matchesFilters(event, appliedFilters),
    );

    // Sort by date, newest first
    return [...filtered].sort((a, b) => {
      const aDate = a.requestEvent?.created || 0;
      const bDate = b.requestEvent?.created || 0;
      return bDate - aDate; // Descending order (newest first)
    });
  }, [initialData, appliedFilters]);

  const getUniqueFilteredValues = useCallback(
    <T extends string>(
      ignoredKey:
        | "statusFilter"
        | "operationFilter"
        | "orgFilter"
        | "resourceFilter",
      selector: (event: IEvent) => T,
    ): T[] => {
      return [
        ...new Set(
          initialData
            .filter((event) =>
              matchesFilters(event, appliedFilters, ignoredKey),
            )
            .map(selector),
        ),
      ].sort((a, b) => a.localeCompare(b));
    },
    [initialData, appliedFilters],
  );

  const uniqueStatus = useMemo<("ok" | "error")[]>(() => {
    const statusOptions: ("ok" | "error")[] = ["ok", "error"];
    const statuses = new Set(
      getUniqueFilteredValues("statusFilter", (event) =>
        event.hasError ? "error" : "ok",
      ),
    );

    return statusOptions.filter((status) => statuses.has(status));
  }, [getUniqueFilteredValues]);

  const uniqueOperation = useMemo<
    ("CREATE" | "UPDATE" | "DELETE" | "VALIDATE" | "UNKNOWN")[]
  >(() => {
    const operationOptions: (
      | "CREATE"
      | "UPDATE"
      | "DELETE"
      | "VALIDATE"
      | "UNKNOWN"
    )[] = ["CREATE", "UPDATE", "DELETE", "VALIDATE", "UNKNOWN"];

    const operations = new Set(
      getUniqueFilteredValues("operationFilter", (event) => {
        const operation = event.requestEvent?.operationType?.toUpperCase();
        return operationOptions.includes(
          operation as "CREATE" | "UPDATE" | "DELETE" | "VALIDATE" | "UNKNOWN",
        )
          ? (operation as
              | "CREATE"
              | "UPDATE"
              | "DELETE"
              | "VALIDATE"
              | "UNKNOWN")
          : "UNKNOWN";
      }),
    );

    return operationOptions.filter((operation) => operations.has(operation));
  }, [getUniqueFilteredValues]);

  const uniqueOrganisation = useMemo(() => {
    return getUniqueFilteredValues("orgFilter", (event) => event.orgId);
  }, [getUniqueFilteredValues]);

  const uniqueResource = useMemo(() => {
    return getUniqueFilteredValues(
      "resourceFilter",
      (event) => event.requestEvent?.resourceName ?? "",
    ).filter((resource) => resource !== "");
  }, [getUniqueFilteredValues]);

  const loadingDetail = false;
  return (
    <Box padding="space-32" paddingBlock="space-8">
      <EventsFilter
        filters={appliedFilters}
        onApplyFilters={handleApplyFilters}
      />
      <EventsTable
        data={filteredData}
        onRowClick={handleRowClick}
        loading={loadingDetail}
        currentPage={currentPage}
        onPageChange={handlePageChange}
        itemsPerPage={itemsPerPage}
        searchFilter={appliedFilters.searchFilter}
        onSearchFilterChange={handleSearchFilterChange}
        uniqueStatus={uniqueStatus}
        uniqueOperation={uniqueOperation}
        uniqueOrg={uniqueOrganisation}
        uniqueResource={uniqueResource}
        activeFilters={{
          status: getStatusFilterValue(appliedFilters.statusFilter),
          operation: getOperationFilterValue(appliedFilters.operationFilter),
          org: appliedFilters.orgFilter,
          resource: appliedFilters.resourceFilter,
        }}
        onStatusFilterChange={handleStatusFilterChange}
        onOperationFilterChange={handleOperationFilterChange}
        onOrgFilterChange={handleOrgFilterChange}
        onResourceFilterChange={handleResourceFilterChange}
        onClearFilters={handleClearFilters}
      />

      {selectedEvent && selectedEvent.requestEvent && (
        <EventsModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          requestData={selectedEvent.requestEvent}
          responseData={selectedEvent.responseEvent}
        />
      )}
    </Box>
  );
}
