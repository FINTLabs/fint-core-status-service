import type { components } from "./schema";

type Schemas = components["schemas"];

export type Dashboard = Schemas["Dashboard"];
export type OrgHealth = Schemas["OrgHealth"];
export type ContractPage = Schemas["ContractPage"];
export type ContractSummary = Schemas["ContractSummary"];
export type ContractDetail = Schemas["ContractDetail"];
export type CapabilityView = Schemas["CapabilityView"];
export type SyncSummary = Schemas["SyncSummary"];
export type SyncDetail = Schemas["SyncDetail"];
export type SyncPage = Schemas["PageResponseSyncSummary"];
export type EventSummary = Schemas["EventSummary"];
export type EventDetail = Schemas["EventDetail"];
export type EventPage = Schemas["PageResponseEventSummary"];
export type OrgNode = Schemas["OrgNode"];
export type ModelDomain = Schemas["ModelDomain"];
export type CountResponse = Schemas["CountResponse"];
export type Info = Schemas["Info"];
export type HeartbeatHealth = ContractSummary["heartbeat"];
export type ContractFilter =
  | "ALL"
  | "HEARTBEAT_STOPPED"
  | "HEARTBEAT_NEVER"
  | "FULL_SYNC_OVERDUE"
  | "FULL_SYNC_NEVER"
  | "MUTED";
