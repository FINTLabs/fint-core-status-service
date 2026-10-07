import { Alert, Box, Heading, VStack } from "@navikt/ds-react";
import type { IStats } from "~/types/Stats";
import { DashboardStats } from "~/components/dashboard/DashboardStats";

interface DashboardPageProps {
  betaStats: IStats | null;
  apiStats: IStats | null;
  alphaStats: IStats | null;
  betaError?: string;
  apiError?: string;
  alphaError?: string;
}

//TODO: fix loading when errors
export function DashboardPage({
  betaStats,
  apiStats,
  alphaStats,
  betaError,
  apiError,
  alphaError,
}: DashboardPageProps) {
  return (
    <VStack
      gap="space-16"
      justify="center"
      className={"w-full"}
      paddingInline="space-96"
    >
      {apiError ? (
        <Box padding="space-16">
          <Heading level="2" size="medium" spacing>
            API
          </Heading>
          <Alert variant="error">{apiError}</Alert>
        </Box>
      ) : apiStats ? (
        <DashboardStats stats={apiStats} env="API" />
      ) : null}
      {betaError ? (
        <Box padding="space-16">
          <Heading level="2" size="medium" spacing>
            BETA
          </Heading>
          <Alert variant="error">{betaError}</Alert>
        </Box>
      ) : betaStats ? (
        <DashboardStats stats={betaStats} env="BETA" />
      ) : null}
      {alphaError ? (
        <Box padding="space-16">
          <Heading level="2" size="medium" spacing>
            ALPHA
          </Heading>
          <Alert variant="error">{alphaError}</Alert>
        </Box>
      ) : alphaStats ? (
        <DashboardStats stats={alphaStats} env="ALPHA" />
      ) : null}
    </VStack>
  );
}
