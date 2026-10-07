// Events Data Structure
export interface IEvent {
  topic: string;
  corrId: string;
  orgId: string;
  hasError: boolean;
  requestEvent: IRequestEvent | null;
  responseEvent: IResponseEvent;
}

export interface IRequestEvent {
  corrId: string;
  orgId: string;
  domainName: string;
  packageName: string;
  resourceName: string;
  operationType: string | null;
  created: number;
  timeToLive: number;
  value: string;
}

export interface IResponseEvent {
  corrId: string;
  orgId: string;
  adapterId: string;
  handledAt: number;
  value: {
    identifier: string | null;
    resource: string;
  };
  operationType: string | null;
  failed: boolean;
  errorMessage: string | null;
  rejected: boolean;
  rejectReason: string | null;
  conflicted: boolean;
  conflictReason: string | null;
}
