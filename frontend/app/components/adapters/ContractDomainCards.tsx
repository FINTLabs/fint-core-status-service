import {
  Detail,
  HGrid,
  HStack,
  InfoCard,
  Label,
  VStack,
} from "@navikt/ds-react";
import {
  CheckmarkCircleFillIcon,
  CheckmarkCircleIcon,
  ExclamationmarkTriangleFillIcon,
} from "@navikt/aksel-icons";
import type { IContractDomain } from "~/types";
import { formatDateRelative } from "~/utils/time";
import { useMemo } from "react";

interface ContractDomainCardsProps {
  data: IContractDomain[];
  onCardClick: (item: IContractDomain) => void;
}

export function ContractDomainCards({
  data,
  onCardClick,
}: ContractDomainCardsProps) {
  // Sort so errors come first: hasContact false, then no syncs, then older syncs
  const sortedData = useMemo(() => {
    return [...data].sort((a, b) => {
      // First priority: hasContact false comes first
      if (!a.hasContact && b.hasContact) return -1;
      if (a.hasContact && !b.hasContact) return 1;

      // Second priority: if both have same contact status, prioritize items with no syncs (0 or missing)
      const aHasNoSyncs =
        (!a.lastDeltaSync || a.lastDeltaSync === 0) &&
        (!a.lastFullSync || a.lastFullSync === 0);
      const bHasNoSyncs =
        (!b.lastDeltaSync || b.lastDeltaSync === 0) &&
        (!b.lastFullSync || b.lastFullSync === 0);
      if (aHasNoSyncs && !bHasNoSyncs) return -1;
      if (!aHasNoSyncs && bHasNoSyncs) return 1;

      // Third priority: older syncs come first (more likely to be problematic)
      const aLatestSync = Math.max(a.lastDeltaSync || 0, a.lastFullSync || 0);
      const bLatestSync = Math.max(b.lastDeltaSync || 0, b.lastFullSync || 0);
      if (aLatestSync === 0 && bLatestSync > 0) return -1;
      if (aLatestSync > 0 && bLatestSync === 0) return 1;
      if (aLatestSync > 0 && bLatestSync > 0) {
        return aLatestSync - bLatestSync; // Older syncs first
      }

      return 0;
    });
  }, [data]);

  function getStatusConfig(hasContact: boolean) {
    if (!hasContact) {
      return {
        icon: (
          <ExclamationmarkTriangleFillIcon
            aria-hidden
            className="text-ax-danger-700"
          />
        ),
        dataColor: "danger" as const,
      };
    } else {
      return {
        icon: (
          <CheckmarkCircleIcon aria-hidden className="text-ax-success-700" />
        ),
        dataColor: "success" as const,
      };
    }
  }

  return (
    <HGrid gap="space-16" columns={{ xs: 1, sm: 2, md: 3, lg: 4 }}>
      {sortedData.map((item, index) => {
        const { icon, dataColor } = getStatusConfig(item.hasContact);
        return (
          <InfoCard
            key={`${item.component}-${index}-${index}`}
            data-color={dataColor}
            onClick={() => onCardClick(item)}
          >
            <InfoCard.Header icon={icon}>
              <InfoCard.Title>
                Ressurs <Detail>{item.component}</Detail>
              </InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
              <VStack gap="space-12">
                <Label>Siste Delta</Label>
                {formatDateRelative(item.lastDeltaSync)}
                <Label>Siste Full</Label>
                {formatDateRelative(item.lastFullSync)}
                <hr />
                <HStack gap="space-8" align="center" paddingBlock={"space-16"}>
                  {item.answersEvents ? (
                    <>
                      <CheckmarkCircleFillIcon
                        color="var(--ax-bg-success-strong)"
                        title="Besvarer hendelser"
                        fontSize="1.25rem"
                      />
                      <Label>Besvarer hendelser</Label>
                    </>
                  ) : (
                    <>
                      <ExclamationmarkTriangleFillIcon
                        color="var(--ax-bg-warning-strong)"
                        title="Besvarer ikke hendelser"
                        fontSize="1.25rem"
                      />
                      <Label>Besvarer ikke hendelser</Label>
                    </>
                  )}
                </HStack>
              </VStack>
            </InfoCard.Content>
          </InfoCard>
        );
      })}
    </HGrid>
  );
}
