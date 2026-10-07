// Import commands.js using ES2015 syntax:
import "./commands";

// Start MSW in Cypress component tests
import { setupWorker } from "msw/browser";
import { handlers } from "../mocks/handlers";

const worker = setupWorker(...handlers);

before(() => {
  worker.start({
    onUnhandledRequest: "bypass",
  });
});

after(() => {
  worker.stop();
});

// Reset handlers between tests
beforeEach(() => {
  worker.resetHandlers();
});
