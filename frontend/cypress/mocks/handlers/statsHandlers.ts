import { http, HttpResponse } from "msw";
import eventsMetrics from "../../fixtures/eventMetrics.json";
import contractStatResponse from "../../fixtures/contractStatResponse.json";
import syncStatsEResponse from "../../fixtures/syncStatsResponse.json";
import { BASE_URL, API_URL, ALPHA_URL } from "./constants";

export const statsHandlers = [
  // Stats API - Beta
  http.get(`${BASE_URL}/event/metrics`, () => {
    return HttpResponse.json(eventsMetrics);
  }),

  http.get(`${BASE_URL}/contract/metrics`, () => {
    return HttpResponse.json(contractStatResponse);
  }),

  http.get(`${BASE_URL}/page-metadata/metrics`, () => {
    return HttpResponse.json(syncStatsEResponse);
  }),

  // Stats API - API
  http.get(`${API_URL}/event/metrics`, () => {
    return HttpResponse.json(eventsMetrics);
  }),

  http.get(`${API_URL}/contract/metrics`, () => {
    return HttpResponse.json(contractStatResponse);
  }),

  http.get(`${API_URL}/page-metadata/metrics`, () => {
    return HttpResponse.json(syncStatsEResponse);
  }),

  // Stats API - ALPHA
  http.get(`${ALPHA_URL}/event/metrics`, () => {
    return HttpResponse.json(eventsMetrics);
  }),

  http.get(`${ALPHA_URL}/contract/metrics`, () => {
    return HttpResponse.json(contractStatResponse);
  }),

  http.get(`${ALPHA_URL}/page-metadata/metrics`, () => {
    return HttpResponse.json(syncStatsEResponse);
  }),
];
