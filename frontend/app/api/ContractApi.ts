import { type ApiResponse, NovariApiManager } from "novari-frontend-components";
import { AuthProperties } from "~/utils/auth";
import type {
  IContractStatus,
  IContractDomain,
  IContractComponent,
} from "~/types";
import { backendRoutesMap } from "./backendRoutes.js";

const apiManagerBeta = new NovariApiManager({
  baseUrl: backendRoutesMap.beta,
});

const apiManagerApi = new NovariApiManager({
  baseUrl: backendRoutesMap.api,
});

const apiManagerAlpha = new NovariApiManager({
  baseUrl: backendRoutesMap.alpha,
});

const apiManagers = {
  beta: apiManagerBeta,
  api: apiManagerApi,
  alpha: apiManagerAlpha,
} as const;

class ContractApi {
  static async getContractStatus(
    env: "beta" | "api" | "alpha" = "api",
  ): Promise<ApiResponse<IContractStatus[]>> {
    const token = AuthProperties.getToken();
    const apiManager = apiManagers[env];

    if (!apiManager) {
      return {
        success: false,
        message: "Ukjent miljø",
        data: [],
        variant: "error",
      };
    }

    const returnValue = await apiManager.call<IContractStatus[]>({
      method: "GET",
      endpoint: `/contract/status`,
      functionName: "getAdapterContract",
      additionalHeaders: {
        Authorization: token,
      },
    });

    if (!returnValue.success && returnValue.status === 401) {
      return {
        success: false,
        message: "Unauthorized",
        data: [],
        variant: "warning",
      };
    }

    return returnValue;
  }

  // /contract/fintlabs.no/domain/personvern
  static async getContractDomain(
    orgId: string,
    domainId: string,
    env: "beta" | "api" | "alpha" = "api",
  ): Promise<ApiResponse<IContractDomain[]>> {
    const token = AuthProperties.getToken();

    const apiManager = apiManagers[env];

    if (!apiManager) {
      return {
        success: false,
        message: "Ukjent miljø",
        data: [],
        variant: "error",
      };
    }

    return await apiManager.call<IContractDomain[]>({
      method: "GET",
      endpoint: `/contract/${orgId}/domain/${domainId}`,
      functionName: "getAdapterContractDetail",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }

  // /contract/fintlabs.no/component/personvern-samtykke
  static async getContractComponent(
    domainId: string,
    componentId: string,
    env: "beta" | "api" | "alpha" = "api",
  ): Promise<ApiResponse<IContractComponent[]>> {
    const token = AuthProperties.getToken();
    const apiManager = apiManagers[env];

    if (!apiManager) {
      return {
        success: false,
        message: "Ukjent miljø",
        data: [],
        variant: "error",
      };
    }

    return await apiManager.call<IContractComponent[]>({
      method: "GET",
      endpoint: `/contract/${domainId}/component/${componentId}`,
      functionName: "getAdapterComponentContract",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }
}

export default ContractApi;
