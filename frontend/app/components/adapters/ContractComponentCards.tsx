import { Box, Detail, HGrid, InfoCard, Label, VStack } from "@navikt/ds-react";
import { HeartBrokenIcon, HeartIcon } from "@navikt/aksel-icons";
import { formatDateRelative } from "~/utils/time";
import type { IContractComponent } from "~/types";
import { useMemo } from "react";

interface ContractComponentCardsProps {
  data: IContractComponent[];
}

export function ContractComponentCards({ data }: ContractComponentCardsProps) {
  // Sort so errors come first: heartbeat false, then lastDelta/lastFull being 0 or very old
  const sortedData = useMemo(() => {
    return [...data].sort((a, b) => {
      // First priority: heartbeat false comes first
      if (!a.heartbeat && b.heartbeat) return -1;
      if (a.heartbeat && !b.heartbeat) return 1;

      // Second priority: if both have same heartbeat status, prioritize items with no syncs (0 or missing)
      const aHasNoSyncs =
        (!a.lastDelta || a.lastDelta === 0) &&
        (!a.lastFull || a.lastFull === 0);
      const bHasNoSyncs =
        (!b.lastDelta || b.lastDelta === 0) &&
        (!b.lastFull || b.lastFull === 0);
      if (aHasNoSyncs && !bHasNoSyncs) return -1;
      if (!aHasNoSyncs && bHasNoSyncs) return 1;

      // Third priority: older syncs come first (more likely to be problematic)
      const aLatestSync = Math.max(a.lastDelta || 0, a.lastFull || 0);
      const bLatestSync = Math.max(b.lastDelta || 0, b.lastFull || 0);
      if (aLatestSync === 0 && bLatestSync > 0) return -1;
      if (aLatestSync > 0 && bLatestSync === 0) return 1;
      if (aLatestSync > 0 && bLatestSync > 0) {
        return aLatestSync - bLatestSync; // Older syncs first
      }

      return 0;
    });
  }, [data]);

  if (sortedData.length === 0) {
    return (
      <Box padding="space-16" borderRadius="8" shadow="dialog">
        <Box className="text-center text-ax-neutral-600">
          Ingen ressurser funnet
        </Box>
      </Box>
    );
  }

  return (
    <HGrid gap="space-16" columns={{ xs: 1, sm: 2, md: 3, lg: 4 }}>
      {sortedData.map((item, index) => {
        const hasError =
          !item.heartbeat ||
          ((!item.lastDelta || item.lastDelta === 0) &&
            (!item.lastFull || item.lastFull === 0));

        return (
          <InfoCard
            key={`${item.adapterId}-${index}-${index}`}
            data-color={hasError ? "danger" : "success"}
          >
            <InfoCard.Header
              icon={hasError ? <HeartBrokenIcon /> : <HeartIcon />}
            >
              <InfoCard.Title>
                Adapter ID <Detail>{item.adapterId}</Detail>
              </InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
              <VStack gap="space-12">
                <Label>Siste Delta</Label>
                {formatDateRelative(item.lastDelta)}
                <Label>Siste Full</Label>
                {formatDateRelative(item.lastFull)}
              </VStack>
            </InfoCard.Content>
          </InfoCard>
        );
      })}
    </HGrid>
  );
}
