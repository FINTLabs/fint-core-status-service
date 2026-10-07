import { type ApiResponse, NovariApiManager } from "novari-frontend-components";
import type { ISyncData } from "~/types";
import { AuthProperties } from "~/utils/auth";
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

class SyncApi {
  static async getAllSync(
    env: "beta" | "api" | "alpha",
    range?: { fromDate?: number; toDate?: number },
  ): Promise<ApiResponse<ISyncData[]>> {
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

    const queryParams = new URLSearchParams();
    if (typeof range?.fromDate === "number") {
      queryParams.set("from", String(range.fromDate));
    }
    if (typeof range?.toDate === "number") {
      queryParams.set("to", String(range.toDate));
    }

    const endpoint = queryParams.size
      ? `/page-metadata?${queryParams.toString()}`
      : "/page-metadata";

    return await apiManager.call<ISyncData[]>({
      method: "GET",
      endpoint,
      functionName: "getAllSync",
      customErrorMessage: "Kunne ikke hente synkroniseringer",
      customSuccessMessage: "Synkroniseringer hentet vellykket",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }
}

export default SyncApi;
