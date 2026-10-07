export interface IContractStatus {
  organzation: string;
  domain: string;
  status: ContractStatusEnum;
}

export enum ContractStatusEnum {
  NOT_FOLLOWING_CONTRACT = "NOT_FOLLOWING_CONTRACT",
  NO_HEARTBEAT = "NO_HEARTBEAT",
  HEALTHY = "HEALTHY",
  UNOWN_STATUS = "UNOWN_STATUS"
}

export interface IContractDomain {
  component: string;
  hasContact: boolean;
  answersEvents: boolean;
  lastDeltaSync: number | null;
  lastFullSync: number | null;
}

export interface IContractComponent {
  adapterId: string;
  heartbeat: boolean;
  lastDelta: number;
  lastFull: number;
}
