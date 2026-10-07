import { adapterHandlers } from "./handlers/adapterHandlers";
import { eventsHandlers } from "./handlers/eventsHandlers";
import { syncHandlers } from "./handlers/syncHandlers";
import { statsHandlers } from "./handlers/statsHandlers";

export const handlers = [
  ...adapterHandlers,
  ...eventsHandlers,
  ...syncHandlers,
  ...statsHandlers,
];
