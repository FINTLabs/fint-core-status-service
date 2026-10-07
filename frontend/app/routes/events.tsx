import { EventsPage } from "~/components/events/EventsPage";
import {
  Await,
  type LoaderFunction,
  useAsyncValue,
  useLoaderData,
  useNavigation,
  useRevalidator,
  useSearchParams,
} from "react-router";
import EventsApi from "~/api/EventsApi";
import { selectedEnvCookie } from "~/utils/cookies";
import {
  NovariSnackbar,
  type NovariSnackbarItem,
} from "novari-frontend-components";
import * as React from "react";
import { Suspense, useEffect, useState } from "react";
import { Alert, Box, Button, Loader } from "@navikt/ds-react";
import { PageHeader } from "~/components/layout/PageHeader";
import { BellIcon, ArrowCirclepathIcon } from "@navikt/aksel-icons";
import type { IEvent } from "~/types/Event";

export function meta() {
  return [
    { title: "Hendelser - Fint Core Status Service" },
    { name: "description", content: "View event logs and operations" },
  ];
}

export const loader: LoaderFunction = async ({ request }) => {
  const cookieHeader = request.headers.get("Cookie");
  const env = await selectedEnvCookie.parse(cookieHeader);
  const url = new URL(request.url);

  const fromParam = url.searchParams.get("from");
  const toParam = url.searchParams.get("to");
  const fromDate =
    fromParam && !Number.isNaN(Number(fromParam))
      ? Number(fromParam)
      : undefined;
  const toDate =
    toParam && !Number.isNaN(Number(toParam)) ? Number(toParam) : undefined;

  const eventResponse = EventsApi.getAllEvents(env, { fromDate, toDate });

  return { env, eventResponse };
};

export default function Events() {
  const { env, eventResponse } = useLoaderData() as {
    env: string;
    eventResponse: Promise<{
      success: boolean;
      message?: string;
      data?: IEvent[];
    }>;
  };

  const [alerts, setAlerts] = useState<NovariSnackbarItem[]>([]);
  const [searchParams, setSearchParams] = useSearchParams();

  const navigation = useNavigation();
  const revalidator = useRevalidator();
  const isNavigating = Boolean(navigation.location);
  const isRefreshing = revalidator.state === "loading";

  const fromParam = searchParams.get("from");
  const toParam = searchParams.get("to");
  const fromTimestamp =
    fromParam && !Number.isNaN(Number(fromParam))
      ? Number(fromParam)
      : undefined;
  const toTimestamp =
    toParam && !Number.isNaN(Number(toParam)) ? Number(toParam) : undefined;

  const dateRange = {
    from:
      typeof fromTimestamp === "number" ? new Date(fromTimestamp) : undefined,
    to: typeof toTimestamp === "number" ? new Date(toTimestamp) : undefined,
  };

  const handleDateRangeChange = (value: {
    from: Date | undefined;
    to: Date | undefined;
  }) => {
    const nextSearchParams = new URLSearchParams(searchParams);

    if (value.from) {
      nextSearchParams.set("from", String(value.from.getTime()));
    } else {
      nextSearchParams.delete("from");
    }

    if (value.to) {
      nextSearchParams.set("to", String(value.to.getTime()));
    } else {
      nextSearchParams.delete("to");
    }

    setSearchParams(nextSearchParams);
  };

  //TODO: change to a better notice
  if (isNavigating) {
    return <Box>Loading stuff...</Box>;
  }

  //TODO: use suspense on all routes
  return (
    <>
      <PageHeader
        title="Hendelser"
        description="Oversikt over hendelser og deres status i Fint Core systemet."
        env={env}
        icon={BellIcon}
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
            <Loader size="3xlarge" title="Laster hendelser ..." />
          </Box>
        }
      >
        <Await
          resolve={eventResponse}
          errorElement={
            <Box className="p-6">
              <Alert variant="error" className="mb-4">
                Kunne ikke hente synkroniseringer.
              </Alert>
            </Box>
          }
        >
          <SyncResolved
            env={env}
            alerts={alerts}
            setAlerts={setAlerts}
            dateRange={dateRange}
            onDateRangeChange={handleDateRangeChange}
          />
        </Await>
      </Suspense>
    </>
  );
}

// ---------- Child component used inside <Await> ----------
function SyncResolved({
  env,
  alerts,
  setAlerts,
  dateRange,
  onDateRangeChange,
}: {
  env: string;
  alerts: NovariSnackbarItem[];
  setAlerts: React.Dispatch<React.SetStateAction<NovariSnackbarItem[]>>;
  dateRange: {
    from: Date | undefined;
    to: Date | undefined;
  };
  onDateRangeChange: (value: {
    from: Date | undefined;
    to: Date | undefined;
  }) => void;
}) {
  const response = useAsyncValue() as {
    success: boolean;
    message?: string;
    data?: IEvent[];
  };

  useEffect(() => {
    if (!response?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `sync-error-${Date.now()}`,
          variant: "error",
          message: response?.message || "Kunne ikke hente hendelser.",
          header: "Connection Feil",
        },
      ]);
    }
  }, [response?.success, response?.message, setAlerts]);

  return (
    <>
      <EventsPage
        initialData={response.data || []}
        env={env}
        dateRange={dateRange}
        onDateRangeChange={onDateRangeChange}
      />
      <NovariSnackbar items={alerts} />
    </>
  );
}
