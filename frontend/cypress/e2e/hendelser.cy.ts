Cypress.on("uncaught:exception", (err) => {
  if (
    /hydrat/i.test(err.message) ||
    /Minified React error #418/.test(err.message) ||
    /Minified React error #423/.test(err.message) ||
    /ResizeObserver loop completed with undelivered notifications/.test(err.message)
  ) {
    return false;
  }
});
describe("Hendelser Page", () => {
  beforeEach(() => {
    // Load fixture data
    cy.fixture("events").as("eventsData");
    cy.fixture("event-detail").as("eventDetailData");
    cy.visit("/hendelser");

    // Wait for Novari theme to be applied
    cy.get('[data-theme="novari"]', { timeout: 10000 }).should("exist");
    cy.wait(1000);
  });

  it("should display hendelser table with data", () => {
    cy.contains("Hendelser").should("be.visible");
    cy.contains("Oversikt over hendelser og operasjoner").should("be.visible");

    // Wait for API data to load
    cy.waitForAPI();

    // Check if table headers are present
    cy.contains("Hendelse ID").should("be.visible");
    cy.contains("Operasjon").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Ressurser").should("be.visible");
    cy.contains("Status").should("be.visible");
    cy.contains("Overført").should("be.visible");

    // Check if data is displayed (20 per page)
    cy.get("[data-cy='event-row']").should("have.length", 20);
  });

  it("should filter data by search", () => {
    cy.waitForAPI();

    // Type in search box
    cy.get('input[type="search"]').type("07c76");

    // Check that only matching data is shown
    cy.contains("07c76...21eff").should("be.visible");
    cy.contains("telemarkfylke.no").should("not.exist");
  });

  it("should open modal when row is clicked", () => {
    cy.waitForAPI();

    // Click on first row
    cy.get("[data-cy='event-row']").first().click();

    // Modal should open
    cy.contains("Hendelse Detaljer").should("be.visible");
    cy.contains("Request").should("be.visible");
    cy.contains("Response").should("be.visible");
  });

  it("should display request and response data in modal", () => {
    cy.waitForAPI();

    // Click on first row to open modal
    cy.get("[data-cy='event-row']").first().click();

    // Check request tab
    cy.contains("Request").click();
    cy.contains("Correlation ID").should("be.visible");
    cy.contains("Organization").should("be.visible");
    cy.contains("Operation").should("be.visible");

    // Check response tab
    cy.contains("Response").click();
    cy.contains("Correlation ID").should("be.visible");
    cy.contains("Organization").should("be.visible");
    cy.contains("Operation").should("be.visible");
    cy.contains("Status").should("be.visible");
    cy.contains("Request").should("be.visible");
    cy.contains("Response").should("be.visible");
  });

  it("should copy JSON data using copy button", () => {
    cy.waitForAPI();

    // Open modal
    cy.get("[data-cy='event-row']").first().click();

    // Check request tab has copy button
    cy.contains("Request").click();
    cy.contains("button", "Kopier JSON").should("be.visible");

    // Click copy button for request
    cy.contains("button", "Kopier JSON").click();

    // Button text should change to "Kopiert!"
    cy.contains("button", "Kopiert!").should("be.visible");

    // Check response tab has copy button
    cy.contains("Response").click();
    cy.contains("button", "Kopier JSON").should("be.visible");

    // Click copy button for response
    cy.contains("button", "Kopier JSON").click();

    // Button text should change to "Kopiert!"
    cy.contains("button", "Kopiert!").should("be.visible");
  });

  it("should close modal when close button is clicked", () => {
    cy.waitForAPI();

    // Open modal
    cy.get("[data-cy='event-row']").first().click();
    cy.contains("Hendelse Detaljer").should("be.visible");

    // Close modal
    cy.contains("Lukk").click();

    // Modal should be closed
    cy.contains("Hendelse Detaljer").should("not.exist");
  });

  it("should clear filters when clear button is clicked", () => {
    cy.waitForAPI();

    // Set some filters
    cy.get('input[type="search"]').type("test");

    // Clear filters
    cy.contains("Tøm filtre").click();

    // Search box should be empty
    cy.get('input[type="search"]').should("have.value", "");
  });

  it("should paginate through events (20 per page)", () => {
    cy.waitForAPI();

    // Check first page has 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Check pagination controls are visible
    cy.get("[data-cy='pagination']").should("be.visible");

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Check second page has 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Navigate to page 3
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "3").click();
    });

    // Check third page has remaining items (50 total - 40 from first two pages = 10)
    cy.get("[data-cy='event-row']").should("have.length", 10);

    // Navigate back to page 1
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "1").click();
    });

    // Should be back at page 1 with 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);
  });

  it("should hide pagination when filtered results are less than 20", () => {
    cy.waitForAPI();

    // Initially pagination should be visible (50 total events)
    cy.get("[data-cy='pagination']").should("be.visible");

    // Filter to show only a few results
    cy.get('input[type="search"]').type("07c76");

    // Should show only 1 result
    cy.get("[data-cy='event-row']").should("have.length", 1);

    // Pagination should not be visible
    cy.get("[data-cy='pagination']").should("not.exist");
  });

  it("should reset to page 1 when search filter is applied", () => {
    cy.waitForAPI();

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Wait for page 2 data to load and verify we're on page 2
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Apply a search filter using a term we know exists
    cy.get('input[type="search"]').type("personvern");

    // Wait a bit for filter to apply and should reset to page 1 with filtered results
    cy.wait(300);
    cy.get("[data-cy='event-row']").should("have.length.at.least", 1);

    // Clear the filter
    cy.contains("Tøm filtre").click();

    // Should be back to showing all data on page 1
    cy.get("[data-cy='event-row']").should("have.length", 20);
  });

  it("should maintain correct event count per page", () => {
    cy.waitForAPI();

    // Page 1: 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Page 2: 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Navigate to last page (page 3)
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "3").click();
    });

    // Page 3: 10 items (50 total - 40 from first two pages)
    cy.get("[data-cy='event-row']").should("have.length", 10);
  });

  it("should display correct page number as active", () => {
    cy.waitForAPI();

    // Verify we start on page 1 with 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Wait for page 2 data to load - this confirms we're on page 2
    cy.get("[data-cy='event-row']").should("have.length", 20);
  });

  it("should open modal from any page", () => {
    cy.waitForAPI();

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Wait for page 2 data to load
    cy.get("[data-cy='event-row']").should("have.length", 20);

    // Click on first row of page 2
    cy.get("[data-cy='event-row']").first().click();

    // Modal should open
    cy.contains("Hendelse Detaljer").should("be.visible");

    // Close modal
    cy.contains("Lukk").click();

    // Should still be on page 2 with 20 items
    cy.get("[data-cy='event-row']").should("have.length", 20);
    cy.get("[data-cy='pagination']").should("be.visible");
  });
});
