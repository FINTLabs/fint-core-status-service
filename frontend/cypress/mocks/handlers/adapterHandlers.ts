import { http, HttpResponse } from "msw";
import contractStatus from "../../fixtures/contract-status.json";
import contractDomain from "../../fixtures/contract-domain.json";
import contractComponent from "../../fixtures/contract-component.json";
import { BASE_URL } from "./constants";

export const adapterHandlers = [
  // Adapter page 1
  http.get(`${BASE_URL}/contract/status`, () => {
    return HttpResponse.json(contractStatus);
  }),

  // /contract/${adapterId}/${componentId} page 2
  http.get(`${BASE_URL}/contract/fintlabs.no/domain/personvern`, () => {
    return HttpResponse.json(contractDomain);
  }),

  // /contract/${orgId}/component/${domainId} page 3
  http.get(
    `${BASE_URL}/contract/fintlabs.no/component/utdanning-kodeverk`,
    () => {
      return HttpResponse.json(contractComponent);
    },
  ),
];
