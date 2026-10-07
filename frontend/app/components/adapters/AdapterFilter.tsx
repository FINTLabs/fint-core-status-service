import {
  Box,
  Button,
  ExpansionCard,
  HGrid,
  HStack,
  Select,
  VStack,
} from "@navikt/ds-react";
import { FunnelIcon } from "@navikt/aksel-icons";
import React from "react";

interface AdaptereFilterProps {
  organisationFilter: string;
  domainFilter: string;
  uniqueOrganisations: string[];
  uniqueDomains: string[];
  onOrganisationFilterChange: (value: string) => void;
  onDomainFilterChange: (value: string) => void;
  onClearFilters: () => void;
}

export function AdapterFilter({
  organisationFilter,
  domainFilter,
  uniqueOrganisations,
  uniqueDomains,
  onOrganisationFilterChange,
  onDomainFilterChange,
  onClearFilters,
}: AdaptereFilterProps) {
  const handleClearFilters = () => {
    // Let parent handle clearing all filter state
    onClearFilters();
  };
  return (
    <Box paddingBlock={"space-24"}>
      <ExpansionCard aria-label="Filtrer adaptere" size="small">
        <ExpansionCard.Header>
          <HStack gap="space-8">
            <FunnelIcon aria-hidden fontSize="1.5rem" />
            <ExpansionCard.Title size="small">Filtrer</ExpansionCard.Title>
          </HStack>
        </ExpansionCard.Header>
        <ExpansionCard.Content>
          <VStack gap="space-16">
            {/* Select Filters Row */}
            <HGrid gap="space-24" columns={2}>
              <Select
                size="small"
                label="Organisasjon"
                value={organisationFilter}
                onChange={(e) => onOrganisationFilterChange(e.target.value)}
                id="organisation-filter"
                data-cy="organisation-filter"
              >
                <option value="">Alle organisasjoner</option>
                {uniqueOrganisations.map((org) => (
                  <option key={org} value={org}>
                    {org}
                  </option>
                ))}
              </Select>

              <Select
                size="small"
                label="Domene"
                value={domainFilter}
                onChange={(e) => onDomainFilterChange(e.target.value)}
                id="domain-filter"
                data-cy="domain-filter"
              >
                <option value="">Alle domener</option>
                {uniqueDomains.map((domain) => (
                  <option key={domain} value={domain}>
                    {domain}
                  </option>
                ))}
              </Select>
            </HGrid>

            {/* Clear Filters Button */}
            <Box>
              <Button
                variant="tertiary"
                size="small"
                onClick={handleClearFilters}
                // data-color={"brand-magenta"}
              >
                Tøm filtre
              </Button>
            </Box>
          </VStack>
        </ExpansionCard.Content>
      </ExpansionCard>
    </Box>
  );
}
