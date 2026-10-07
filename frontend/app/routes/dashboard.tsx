import { DashboardPage } from "~/components/dashboard/DashboardPage";
import {
  Await,
  type LoaderFunction,
  useAsyncValue,
  useLoaderData,
  useNavigation,
  useRevalidator,
} from "react-router";
import StatsApi from "~/api/StatsApi";
import {
  NovariSnackbar,
  type NovariSnackbarItem,
} from "novari-frontend-components";
import * as React from "react";
import { Suspense, useEffect, useState } from "react";
import { Alert, Box, Button, Loader } from "@navikt/ds-react";
import { PageHeader } from "~/components/layout/PageHeader";
import { DonutChartIcon, ArrowCirclepathIcon } from "@navikt/aksel-icons";
import type { IStats } from "~/types/Stats";
import {AuthProperties} from "~/utils/auth";

export function meta() {
  return [
    { title: "Dashboard - Fint Core Status Service" },
    { name: "description", content: "View statistics dashboard" },
  ];
}

export const loader: LoaderFunction = async () => {
  const statsPromises = StatsApi.getAllStats();

  return {
    betaStats: (await statsPromises).beta,
    apiStats: (await statsPromises).api,
    alphaStats: (await statsPromises).alpha,
  };
};

export default function Dashboard() {
  const { betaStats, apiStats, alphaStats } = useLoaderData() as {
    betaStats: Promise<{ success: boolean; message?: string; data?: IStats }>;
    apiStats: Promise<{ success: boolean; message?: string; data?: IStats }>;
    alphaStats: Promise<{ success: boolean; message?: string; data?: IStats }>;
  };

  const [alerts, setAlerts] = useState<NovariSnackbarItem[]>([]);

  const navigation = useNavigation();
  const revalidator = useRevalidator();
  const isNavigating = Boolean(navigation.location);
  const isRefreshing = revalidator.state === "loading";

  if (isNavigating) {
    return <Box>Loading stuff...</Box>;
  }

  return (
    <>
      <PageHeader
        title="Dashboard"
        description="Oversikt over statistikk fra alle miljøer i Fint Core systemet."
        icon={DonutChartIcon}
        actions={
          <Button
            variant="secondary"
            size="small"
            onClick={() => revalidator.revalidate()}
            loading={isRefreshing}
            icon={<ArrowCirclepathIcon aria-hidden />}
          >
            Oppdater
          </Button>
        }
      />
      <Suspense
        fallback={
          <Box className="p-6 flex justify-center">
            <Loader size="3xlarge" title="Laster statistikk ..." />
          </Box>
        }
      >
        <Await
          resolve={Promise.all([betaStats, apiStats, alphaStats])}
          errorElement={
            <Box className="p-6">
              <Alert variant="error" className="mb-4">
                Kunne ikke hente statistikk.
              </Alert>
            </Box>
          }
        >
          <DashboardResolved alerts={alerts} setAlerts={setAlerts} />
        </Await>
      </Suspense>
    </>
  );
}

// ---------- Child component used inside <Await> ----------
function DashboardResolved({
  alerts,
  setAlerts,
}: {
  alerts: NovariSnackbarItem[];
  setAlerts: React.Dispatch<React.SetStateAction<NovariSnackbarItem[]>>;
}) {
  const responses = useAsyncValue() as [
    { success: boolean; message?: string; data?: IStats },
    { success: boolean; message?: string; data?: IStats },
    { success: boolean; message?: string; data?: IStats },
  ];

  const [betaResponse, apiResponse, alphaResponse] = responses;

  useEffect(() => {
    if (!betaResponse?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `beta-stats-error-${Date.now()}`,
          variant: "error",
          message:
            betaResponse?.message || "Kunne ikke hente statistikk fra BETA.",
          header: "BETA Feil",
        },
      ]);
    }
    if (!apiResponse?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `api-stats-error-${Date.now()}`,
          variant: "error",
          message:
            apiResponse?.message || "Kunne ikke hente statistikk fra API.",
          header: "API Feil",
        },
      ]);
    }
    if (!alphaResponse?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `alpha-stats-error-${Date.now()}`,
          variant: "error",
          message:
            alphaResponse?.message || "Kunne ikke hente statistikk fra ALPHA.",
          header: "ALPHA Feil",
        },
      ]);
    }
  }, [
    betaResponse?.success,
    apiResponse?.success,
    alphaResponse?.success,
    betaResponse?.message,
    apiResponse?.message,
    alphaResponse?.message,
    setAlerts,
  ]);

  return (
    <>
      <DashboardPage
        betaStats={betaResponse?.success ? betaResponse.data || null : null}
        apiStats={apiResponse?.success ? apiResponse.data || null : null}
        alphaStats={alphaResponse?.success ? alphaResponse.data || null : null}
        betaError={betaResponse?.success ? undefined : betaResponse?.message}
        apiError={apiResponse?.success ? undefined : apiResponse?.message}
        alphaError={alphaResponse?.success ? undefined : alphaResponse?.message}
      />
      <NovariSnackbar items={alerts} />
    </>
  );
}
