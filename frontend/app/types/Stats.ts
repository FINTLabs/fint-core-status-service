export interface ContractsStatsResponse {
  ContractsMetrics: Record<string, number>;
}

export interface EventStatsResponse {
  EventsMetrics: Record<string, number>;
}

export interface SyncStatsResponse {
  SyncMetrics: Record<string, number>;
}

export interface IStats {
  ContractsMetrics: Record<string, number>;
  EventsMetrics: Record<string, number>;
  SyncMetrics: Record<string, number>;
}