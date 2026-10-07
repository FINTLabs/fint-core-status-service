import { DateFilter } from "~/components/common/DateFilter";

interface SyncFilterProps {
  filters: {
    syncTypeFilter: {
      full: boolean;
      delta: boolean;
    };
    statusFilter: {
      finished: boolean;
      ongoing: boolean;
    };
    orgFilter: string;
    domainFilter: string;
    packageFilter: string;
    resourceFilter: string;
    adapterIdFilter: string;
    dateRange: { from: Date | undefined; to: Date | undefined };
  };
  onApplyFilters: (value: SyncFilterProps["filters"]) => void;
}

export function SyncFilter({ filters, onApplyFilters }: SyncFilterProps) {
  return (
    <DateFilter
      dateRange={filters.dateRange}
      compactActions
      onApplyDateRange={(dateRange) => {
        onApplyFilters({
          ...filters,
          dateRange,
        });
      }}
    />
  );
}
