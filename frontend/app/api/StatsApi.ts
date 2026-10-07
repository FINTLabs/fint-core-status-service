import {type ApiResponse, NovariApiManager} from "novari-frontend-components";

import {AuthProperties} from "~/utils/auth";
import type {ContractsStatsResponse, EventStatsResponse, IStats, SyncStatsResponse} from "~/types/Stats";
import {backendRoutesMap} from "./backendRoutes.js";

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

class StatsApi {
  static async getContractMetrics(
    env: "beta" | "api" | "alpha",
    token: string
  ): Promise<ApiResponse<ContractsStatsResponse>> {
    const apiManager = apiManagers[env];
    console.log("env", token)

    if (!apiManager) {
      return {
        success: false,
        message: "Ukjent miljø",
        data: undefined,
        variant: "error",
      };
    }

    return await apiManager.call<ContractsStatsResponse>({
      method: "GET",
      endpoint: "/contract/contract-metrics",
      functionName: "getContractMetrics",
      customErrorMessage: "Kunne ikke hente statistikk for kontrakter",
      customSuccessMessage: "Statistikk hentet vellykket",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }

  static async getEventsMetrics(
    env: "beta" | "api" | "alpha",
    token: string
  ): Promise<ApiResponse<EventStatsResponse>> {
    const apiManager = apiManagers[env];

    if (!apiManager) {
      return {
        success: false,
        message: "Ukjent miljø",
        data: undefined,
        variant: "error",
      };
    }

    return await apiManager.call<EventStatsResponse>({
      method: "GET",
      endpoint: "/event/event-metrics",
      functionName: "getEventsMetrics",
      customErrorMessage: "Kunne ikke hente statistikk for eventer",
      customSuccessMessage: "Eventstatistikk hentet vellykket",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }

  static async getSyncMetrics(
    env: "beta" | "api" | "alpha",
    token: string
  ): Promise<ApiResponse<SyncStatsResponse>> {
    const apiManager = apiManagers[env];

    if (!apiManager){
      return {
        success: false,
        message: "Ukjent miljø",
        data: undefined,
        variant: "error",
      };
    }

    return await apiManager.call<SyncStatsResponse>({
      method: "GET",
      endpoint: "/page-metadata/sync-metrics",
      functionName: "getSyncMetrics",
      customErrorMessage: "Kunne ikke hente statistikk for synkronisering",
      customSuccessMessage: "Synkroniseringsstatistikk hentet vellykket",
      additionalHeaders: {
        Authorization: token,
      },
    });
  }

  static async getStatsForEnv(
    env: "beta" | "api" | "alpha",
    token: string
  ): Promise<ApiResponse<IStats>> {
    const [contractResponse, eventResponse, syncResponse] = await Promise.all([
      this.getContractMetrics(env, token),
      this.getEventsMetrics(env, token),
      this.getSyncMetrics(env, token)
    ]);

    if (!contractResponse.success || !contractResponse.data) {
      return {
        success: false,
        status: syncResponse.status,
        message: contractResponse.message ?? "Kunne ikke hente kontraktstatistikk",
        data: undefined,
        variant: "error",
      };
    }

    if (!eventResponse.success || !eventResponse.data) {
      return {
        success: false,
        status: syncResponse.status,
        message: eventResponse.message ?? "Kunne ikke hente eventstatistikk",
        data: undefined,
        variant: "error",
      };
    }

    if (!syncResponse.success || !syncResponse.data) {
      return {
        success: false,
        status: syncResponse.status,
        message: syncResponse.message ?? "Kunne ikke hente syncstatistikk",
        data: undefined,
        variant: "error",
      };
    }

    return {
      success: true,
      message: "great success",
      variant: "success",
      data: {
        ContractsMetrics: contractResponse.data.ContractsMetrics,
        EventsMetrics: eventResponse.data.EventsMetrics,
        SyncMetrics: syncResponse.data.SyncMetrics,
      },
    };
  }

  static async getAllStats(): Promise<{
    beta: ApiResponse<IStats>;
    api: ApiResponse<IStats>;
    alpha: ApiResponse<IStats>;
  }> {
    const token = AuthProperties.getToken();
    const [beta, api, alpha] = await Promise.all([
      this.getStatsForEnv("beta", token),
      this.getStatsForEnv("api", token),
      this.getStatsForEnv("alpha", token),
    ]);
    if (!beta.success || !api.success || !alpha.success) {
      console.error("Failed to get stats for one or more environments", "Api:", api.status, "Beta:", beta.status, "Alpha:", alpha.status);
      console.error("Api message:", api.body, beta.body, alpha.body);
    }

    return {
      beta,
      api,
      alpha,
    };
  }
}

export default StatsApi;

