import type { Route } from "./+types/adapter.$orgId.$domain";
import * as React from "react";
import { Suspense, useEffect, useState } from "react";
import {
  Await,
  type LoaderFunction,
  useAsyncValue,
  useLoaderData,
  useNavigate,
  useNavigation,
} from "react-router";
import type { IContractDomain } from "~/types";
import { PageHeader } from "~/components/layout/PageHeader";
import ContractApi from "~/api/ContractApi";
import { selectedEnvCookie } from "~/utils/cookies";
import { Alert, Box, Loader } from "@navikt/ds-react";
import { LayersIcon } from "@navikt/aksel-icons";
import {
  NovariSnackbar,
  type NovariSnackbarItem,
} from "novari-frontend-components";
import { ContractDomainCards } from "~/components/adapters/ContractDomainCards";
import { Breadcrumbs } from "~/components/layout/Breadcrumbs";

//TODO: fix all meta data
export function meta({ params }: Route.MetaArgs) {
  return [
    {
      title: `Status adaptere pr komponent - ${params.orgId} - Fint Core Status Service`,
    },
    { name: "description", content: "View detailed adapter component status" },
  ];
}

export const loader: LoaderFunction = async ({ request, params }) => {
  const cookieHeader = request.headers.get("Cookie");
  const env = (await selectedEnvCookie.parse(cookieHeader)) || "api";
  const { orgId, domain } = params;

  const response = ContractApi.getContractDomain(
    orgId || "",
    domain || "",
    env,
  );

  return {
    env,
    orgId,
    domain,
    response,
  };
};

export default function AdapterDetail() {
  const { env, orgId, domain, response } = useLoaderData() as {
    env: string;
    orgId: string;
    domain: string;
    response: Promise<{
      success: boolean;
      message?: string;
      data?: IContractDomain[];
      status?: boolean;
      variant?: string;
    }>;
  };

  const [alerts, setAlerts] = useState<NovariSnackbarItem[]>([]);
  const navigation = useNavigation();
  const nav = useNavigate();
  const isNavigating = Boolean(navigation.location);

  function handleCardClick(item: IContractDomain) {
    nav(`/contract/${orgId}/${item.component}?domain=${domain}`);
  }

  if (isNavigating) {
    return <Box>Loading stuff...</Box>;
  }

  const breadcrumbItems = [{ label: `${orgId} - ${domain}`, href: "" }];

  return (
    <>
      <Box padding="space-32" paddingBlock="space-8">
        <PageHeader
          title="Status adaptere pr komponent"
          description={`Komponenter for ${orgId} : ${domain}`}
          env={env}
          icon={LayersIcon}
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
            <AdapterDetailResolved
              setAlerts={setAlerts}
              handleCardClick={handleCardClick}
            />
          </Await>
        </Suspense>
      </Box>
      <NovariSnackbar items={alerts} />
    </>
  );
}

// ---------- Child component used inside <Await> ----------
function AdapterDetailResolved({
  setAlerts,
  handleCardClick,
}: {
  setAlerts: React.Dispatch<React.SetStateAction<NovariSnackbarItem[]>>;
  handleCardClick: (item: IContractDomain) => void;
}) {
  const response = useAsyncValue() as {
    success: boolean;
    message?: string;
    data?: IContractDomain[];
    status?: boolean;
    variant?: string;
  };

  useEffect(() => {
    if (!response?.success) {
      setAlerts((prev) => [
        ...prev,
        {
          id: `adapter-detail-error-${Date.now()}`,
          variant: (response?.variant || "error") as
            | "error"
            | "warning"
            | "success"
            | "info",
          message: response?.message || "Kunne ikke hente adapter detaljer",
          header: "Connection Feil",
        },
      ]);
    }
  }, [response?.success, response?.message, response?.variant, setAlerts]);

  return (
    <ContractDomainCards
      data={response.data || []}
      onCardClick={handleCardClick}
    />
  );
}
