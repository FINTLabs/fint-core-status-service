import { http, HttpResponse, delay } from "msw";
import syncData from "../../fixtures/sync.json";
import syncDataApi from "../../fixtures/sync-api.json";
import { BASE_URL } from "./constants";

export const syncHandlers = [
  // Sync API
  http.get(`${BASE_URL}/page-metadata`, async () => {
    // await delay(5000);
    return HttpResponse.json(syncData);
  }),

  http.get(`https://core-status-beta.fintlabs.no/page-metadata`, async () => {
    await delay(5000);
    return HttpResponse.json(syncDataApi);
  }),
];
