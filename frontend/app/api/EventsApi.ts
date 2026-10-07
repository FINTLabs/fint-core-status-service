import { type ApiResponse, NovariApiManager } from "novari-frontend-components";

import { AuthProperties } from "~/utils/auth";
import type { IEvent } from "~/types/Event";
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

class EventsApi {
  static async getAllEvents(
    env: "beta" | "api" | "alpha" = "api",
    range?: { fromDate?: number; toDate?: number },
  ): Promise<ApiResponse<IEvent[]>> {
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
      ? `/event?${queryParams.toString()}`
      : "/event";

    return await apiManager.call<IEvent[]>({
      method: "GET",
      endpoint,
      functionName: "getAllEvents",
      customErrorMessage: "Kunne ikke hente hendelser",
      customSuccessMessage: "Hendelser hentet vellykket",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }

  // static async getEventDetail(
  //   eventId: string,
  //   env: "beta" | "api" | "alpha" = "api"
  // ): Promise<ApiResponse<IEventDetail>> {
  //   const token = AuthProperties.getToken();
  //   const apiManager = apiManagers[env];
  //
  //   if (!apiManager) {
  //     return {
  //       success: false,
  //       message: "Ukjent miljø",
  //       data: undefined,
  //       variant: "error",
  //     };
  //   }
  //
  //   return await apiManager.call<IEventDetail>({
  //     method: "GET",
  //     endpoint: `/event/${eventId}/detail`,
  //     functionName: "getEventDetail",
  //     customErrorMessage: "Kunne ikke hente hendelse detaljer",
  //     customSuccessMessage: "Hendelse detaljer hentet vellykket",
  //     additionalHeaders: {
  //       Authorization: token,
  //     },
  //   });
  // }
}

export default EventsApi;
