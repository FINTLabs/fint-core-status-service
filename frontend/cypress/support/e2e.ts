// Import commands.js using ES2015 syntax:
import "./commands";

// Alternatively you can use CommonJS syntax:
// require('./commands')

// Start MSW in Cypress
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
