import {
  Box,
  Detail,
  HGrid,
  HStack,
  InfoCard,
  Label,
  VStack,
} from "@navikt/ds-react";
import {
  CheckmarkCircleIcon,
  ExclamationmarkTriangleFillIcon,
  HeartBrokenIcon,
} from "@navikt/aksel-icons";
import type { IContractStatus } from "~/types";

interface AdapterCardsProps {
  data: IContractStatus[];
  onCardClick: (adapter: IContractStatus) => void;
}

function getStatusConfig(status: IContractStatus["status"]) {
  if (status === "NO_HEARTBEAT") {
    return {
      icon: <HeartBrokenIcon aria-hidden className="text-ax-danger-700" />,
      dataColor: "danger" as const,
    };
  }
  if (status === "HEALTHY" || status === "NOT_FOLLOWING_CONTRACT") {
    return {
      icon: <CheckmarkCircleIcon aria-hidden className="text-ax-success-700" />,
      dataColor: "success" as const,
    };
  }
  return {
    icon: <CheckmarkCircleIcon aria-hidden className="text-ax-neutral-700" />,
    dataColor: "info" as const,
  };
}

export function AdapterCards({ data, onCardClick }: AdapterCardsProps) {
  return (
    <HGrid gap="space-16" columns={{ xs: 1, sm: 2, md: 3, lg: 4 }}>
      {data.map((item, index) => {
        const { icon, dataColor } = getStatusConfig(item.status);
        return (
          <InfoCard
            key={`${item.organzation}-${item.domain}-${index}`}
            data-color={dataColor}
            onClick={() => onCardClick(item)}
          >
            <InfoCard.Header icon={icon}>
              <InfoCard.Title>
                {item.organzation}

                <Detail>{item.domain}</Detail>
              </InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
              <VStack gap="space-12">
                <Box>
                  <Label>Organisasjon</Label>
                  <Detail weight="semibold">{item.organzation}</Detail>
                </Box>
                <Box>
                  <Label>Domene</Label>
                  <Detail weight="semibold">{item.domain}</Detail>
                </Box>
                <Box>
                  <Label>Status</Label>
                  <Detail>
                    <HStack gap="space-8" align="center" data-color="warning">
                      {item.status === "NOT_FOLLOWING_CONTRACT" && (
                        <ExclamationmarkTriangleFillIcon
                          title="a11y-title"
                          fontSize="1.5rem"
                          color="var(--ax-bg-warning-strong)"
                        />
                      )}

                      {item.status}
                    </HStack>
                  </Detail>
                </Box>
              </VStack>
            </InfoCard.Content>
          </InfoCard>
        );
      })}
    </HGrid>
  );
}
