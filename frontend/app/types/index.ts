export interface IUserSession {
  selectedEnv: "beta" | "api" | "alpha";
}
export type { IEvent, IRequestEvent, IResponseEvent } from "./Event";
export type { ISyncData, ISyncPage } from "./Sync";
export type { IContractStatus, IContractDomain, IContractComponent } from "./Contract";
export type { IStats } from "./Stats";
