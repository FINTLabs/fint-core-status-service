import { useMemo, useState } from "react";
import { Box } from "@navikt/ds-react";
import { AdapterFilter } from "./AdapterFilter";
import { AdapterCards } from "./AdapterCards";
import type { IContractStatus } from "~/types";
import { useNavigate } from "react-router";

interface AdapterPageProps {
  initialData: IContractStatus[];
}

export function AdapterPage({ initialData }: AdapterPageProps) {
  const nav = useNavigate();

  const [organisationFilter, setOrganisationFilter] = useState<string>("");
  const [domainFilter, setDomainFilter] = useState<string>("");

  function handleCardClick(item: IContractStatus) {
    nav(`/adaptere/${item.organzation}/${item.domain}`);
  }

  const handleOrganisationFilterChange = (value: string) => {
    setOrganisationFilter(value);
  };

  const handleDomainFilterChange = (value: string) => {
    setDomainFilter(value);
  };

  const handleClearFilters = () => {
    setOrganisationFilter("");
    setDomainFilter("");
  };

  const tableData: IContractStatus[] = useMemo(() => {
    return initialData || [];
  }, [initialData]);

  const uniqueOrganisations = useMemo(() => {
    return [
      ...new Set((initialData || []).map((adapter) => adapter.organzation)),
    ].sort();
  }, [initialData]);

  const uniqueDomains = useMemo(() => {
    return [
      ...new Set((initialData || []).map((adapter) => adapter.domain)),
    ].sort();
  }, [initialData]);

  const filteredData = useMemo(() => {
    const filtered = tableData.filter((item) => {
      if (
        organisationFilter &&
        !item.organzation
          .toLowerCase()
          .includes(organisationFilter.toLowerCase())
      ) {
        return false;
      }

      return !(
        domainFilter &&
        !item.domain.toLowerCase().includes(domainFilter.toLowerCase())
      );
    });

    // Sort so errors (NO_HEARTBEAT status) appear first
    return [...filtered].sort((a, b) => {
      // If one has error status and the other doesn't, error comes first
      if (a.status === "NO_HEARTBEAT" && b.status !== "NO_HEARTBEAT") return -1;
      if (a.status !== "NO_HEARTBEAT" && b.status === "NO_HEARTBEAT") return 1;
      // If both have same status, maintain original order (or sort by organization/domain)
      return 0;
    });
  }, [tableData, organisationFilter, domainFilter]);

  return (
    <Box padding="space-32" paddingBlock="space-8">
      <AdapterFilter
        organisationFilter={organisationFilter}
        domainFilter={domainFilter}
        uniqueOrganisations={uniqueOrganisations}
        uniqueDomains={uniqueDomains}
        onOrganisationFilterChange={handleOrganisationFilterChange}
        onDomainFilterChange={handleDomainFilterChange}
        onClearFilters={handleClearFilters}
      />
      <AdapterCards data={filteredData} onCardClick={handleCardClick} />
    </Box>
  );
}
