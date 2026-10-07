import type { Route } from "./+types/contract.$orgId.$componentId";
import * as React from "react";
import { Suspense, useEffect, useState } from "react";
import {
  Await,
  type LoaderFunction,
  useAsyncValue,
  useLoaderData,
  useNavigation,
} from "react-router";
import type { IContractComponent } from "~/types";
import { PageHeader } from "~/components/layout/PageHeader";
import ContractApi from "~/api/ContractApi";
import { selectedEnvCookie } from "~/utils/cookies";
import { Alert, Box, Loader } from "@navikt/ds-react";
import { InformationSquareIcon } from "@navikt/aksel-icons";
import {
  NovariSnackbar,
  type NovariSnackbarItem,
} from "novari-frontend-components";
import { ContractComponentCards } from "~/components/adapters/ContractComponentCards";
import { Breadcrumbs } from "~/components/layout/Breadcrumbs";

//TODO: fix all meta data
export function meta({ params }: Route.MetaArgs) {
  return [
    {
      title: `Adapter Komponent - ${params.componentId} - Fint Core Status Service`,
    },
    {
      name: "description",
      content: "View detailed adapter component information",
    },
  ];
}

export const loader: LoaderFunction = async ({ request, params }) => {
  const cookieHeader = request.headers.get("Cookie");
  const env = (await selectedEnvCookie.parse(cookieHeader)) || "api";
  const { orgId, componentId } = params;
  const url = new URL(request.url);
  const domainId = url.searchParams.get("domain") || "";

  const response = ContractApi.getContractComponent(
    orgId || "",
    componentId || "",
    env,
  );

  return {
    env,
    orgId,
    componentId,
    domainId,
    response,
  };
};

export default function ContractComponent() {
  const { env, orgId, componentId, response, domainId } = useLoaderData() as {
    env: string;
    orgId: string;
    componentId: string;
    domainId: string;
    response: Promise<{
      success: boolean;
      message?: string;
      data?: IContractComponent[];
      variant?: string;
    }>;
  };

  const [alerts, setAlerts] = useState<NovariSnackbarItem[]>([]);

  const navigation = useNavigation();
  const isNavigating = Boolean(navigation.location);

  if (isNavigating) {
    return <Box>Loading stuff...</Box>;
  }

  const breadcrumbItems = [
    {
      label: `${orgId} - ${domainId} `,
      href: `/adaptere/${orgId}/${domainId}`,
    },
    { label: `${componentId}`, href: "" },
  ];

  return (
    <>
      <Box padding="space-32" paddingBlock="space-8">
        <PageHeader
          title="Detaljer for komponent "
          description={`Detaljer for  ${orgId} : ${componentId} `}
          env={env}
          icon={InformationSquareIcon}
        />
        <Breadcrumbs items={breadcrumbItems} />
        <Suspense
          fallback={
            <Box className="p-6 flex justify-center">
              <Loader size="3xlarge" title="Laster adaptere ..." />
            </Box>
          }
        >
          <Await
            resolve={response}
            errorElement={
              <Box className="p-6">
                <Alert variant="error" className="mb-4">
                  Kunne ikke hente adaptere.
                </Alert>
              </Box>
            }
          >
            <ContractComponentResolved setAlerts={setAlerts} />
          </Await>
        </Suspense>
        <NovariSnackbar items={alerts} />
      </Box>
    </>
  );
}

// ---------- Child component used inside <Await> ----------
function ContractComponentResolved({
  setAlerts,
}: {
  setAlerts: React.Dispatch<React.SetStateAction<NovariSnackbarItem[]>>;
}) {
  const response = useAsyncValue() as {
    success: boolean;
    message?: string;
    data?: IContractComponent[];
    variant?: string;
  };

  useEffect(() => {
    if (!response?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `component-error-${Date.now()}`,
          variant: (response?.variant || "error") as
            | "error"
            | "warning"
            | "success"
            | "info",
          message:
            response?.message || "Kunne ikke hente adapter komponent detaljer",
          header: "Connection Feil",
        },
      ]);
    }
  }, [response?.success, response?.message, response?.variant, setAlerts]);

  return <ContractComponentCards data={response.data || []} />;
}
