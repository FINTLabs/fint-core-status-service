// Sync Data Structure
export interface ISyncPage {
  pageNumber: number;
  pageSize: number;
  time: number;
}

export interface ISyncData {
  corrId: string;
  adapterId: string;
  orgId: string;
  domain: string;
  package: string;
  resource: string;
  totalPages: number;
  pagesAcquired: number;
  totalEntities: number;
  entitiesAquired: number;
  syncType: "FULL" | "DELTA";
  pages: ISyncPage[];
  finished: boolean;
  lastPageTime: number;
}

export interface SyncFiltersState {
  syncTypeFilter: {
    full: boolean;
    delta: boolean;
  };
  statusFilter: {
    finished: boolean;
    ongoing: boolean;
  };
  orgFilter: string;
  domainFilter: string;
  packageFilter: string;
  resourceFilter: string;
  adapterIdFilter: string;
  dateRange: {
    from: Date | undefined;
    to: Date | undefined;
  };
}
