/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Custom command to select DOM element by data-cy attribute.
       * @example cy.dataCy('greeting')
       */
      dataCy(value: string): Chainable<JQuery<HTMLElement>>;

      /**
       * Custom command to wait for API calls to complete
       * @example cy.waitForAPI()
       */
      waitForAPI(): Chainable<void>;
    }
  }
}

Cypress.Commands.add("dataCy", (value) => {
  return cy.get(`[data-cy=${value}]`);
});

Cypress.Commands.add("waitForAPI", () => {
  // Wait for MSW to be ready
  cy.window().should("have.property", "__mswReady", true);
});

export {};
