import { http, HttpResponse } from "msw";
import eventsData from "../../fixtures/events.json";
import eventDetailData from "../../fixtures/event-detail.json";
import { BASE_URL } from "./constants";

export const eventsHandlers = [
  // Events API
  http.get(`${BASE_URL}/event`, () => {
    return HttpResponse.json(eventsData);
  }),

  // Event detail API (for modal)
  http.get(`${BASE_URL}/event/:eventId`, () => {
    return HttpResponse.json(eventDetailData);
  }),

  // Event detail API (new endpoint)
  http.get(`${BASE_URL}/event/:eventId/detail`, () => {
    return HttpResponse.json(eventDetailData);
  }),
];
