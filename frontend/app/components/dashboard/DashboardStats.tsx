import {
  Box,
  Detail,
  Heading,
  HGrid,
  HStack,
  InfoCard,
  VStack,
} from "@navikt/ds-react";
import {
  ArrowRightLeftIcon, ArrowsCirclepathIcon,
  ArrowsSquarepathIcon,
  ExclamationmarkTriangleIcon,
  HeartIcon,
  SealCheckmarkFillIcon,
  TasklistIcon, TasklistSaveIcon, TasklistSendIcon,
} from "@navikt/aksel-icons";

import type {IStats} from "~/types";

interface DashboardStatsProps {
  stats: IStats;
  env: string;
}

export function DashboardStats({stats, env}: DashboardStatsProps) {
  const totalContracts = stats.ContractsMetrics?.["total"] ?? 0;
  const noContactContracts = stats.ContractsMetrics?.["no contact"] ?? 0;
  const totalEvents = stats.EventsMetrics?.["total"] ?? 0;
  const eventErrors = stats.EventsMetrics?.["errors"] ?? 0;
  const fullSyncs = stats.SyncMetrics?.["full"] ?? 0;
  const deltaSyncs = stats.SyncMetrics?.["delta"] ?? 0;

  return (
    <InfoCard data-color="brand-magenta">
      <InfoCard.Header icon={<SealCheckmarkFillIcon/>}>
        <InfoCard.Title>Miljø Oversikt - {env}</InfoCard.Title>
      </InfoCard.Header>
      <InfoCard.Content className={"full-width"}>
        <HGrid gap="space-24" columns={3}>
          <VStack gap={"space-16"}>
            <Box
              padding={"space-16"}
              borderRadius="12"
              background={"neutral-soft"}
            >
              <Heading align="center" size="medium" spacing>
                Kontrakter
              </Heading>
              <HStack gap={"space-8"} justify={"center"}>
                <HeartIcon title="Healty heartbeats" fontSize="1.5rem"/>
                {totalContracts}
                <TasklistIcon title="Total Contracts" fontSize="1.5rem"/>
                {noContactContracts}
              </HStack>
            </Box>
          </VStack>

          <VStack gap="space-16">
            <Box
              padding={"space-16"}
              borderRadius="12"
              background={"neutral-soft"}
            >
              <Heading align="center" size="medium" spacing>
                Hendelser
              </Heading>
              <HStack gap={"space-8"} justify={"center"}>
                <ArrowRightLeftIcon title="Total requests" fontSize="1.5rem"/>
                {totalEvents}
                <ExclamationmarkTriangleIcon title="Errors" fontSize="1.5rem"/>
                {eventErrors}
              </HStack>
            </Box>
          </VStack>

          <Box
            padding={"space-16"}
            borderRadius="12"
            background={"neutral-soft"}
          >
            <Heading align="center" size="medium">
              Synkroniseringer
            </Heading>
            <HStack gap={"space-8"} justify={"center"}>
              <TasklistSaveIcon title="Full-syncs" fontSize="1.5rem"/>
              {fullSyncs}
              <TasklistSendIcon title="Delta-syncs" fontSize="1.5rem"/>
              {deltaSyncs}
            </HStack>
          </Box>
        </HGrid>
      </InfoCard.Content>
    </InfoCard>
  );
}
