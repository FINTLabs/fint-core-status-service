import { DateFilter } from "~/components/common/DateFilter";

export interface EventsFilters {
  searchFilter: string;
  dateRange: { from: Date | undefined; to: Date | undefined };
  operationFilter: {
    CREATE: boolean;
    UPDATE: boolean;
    DELETE: boolean;
    VALIDATE: boolean;
    UNKNOWN: boolean;
  };
  orgFilter: string;
  resourceFilter: string;
  statusFilter: {
    ok: boolean;
    error: boolean;
  };
}

interface EventsFilterProps {
  filters: EventsFilters;
  onApplyFilters: (value: EventsFilters) => void;
}

export function EventsFilter({ filters, onApplyFilters }: EventsFilterProps) {
  return (
    <DateFilter
      dateRange={filters.dateRange}
      fromDate={new Date("2020-01-01")}
      toDate={new Date("2030-12-31")}
      onApplyDateRange={(dateRange) => {
        onApplyFilters({
          ...filters,
          dateRange,
        });
      }}
    />
  );
}
