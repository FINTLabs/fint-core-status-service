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

describe("Adapter Page", () => {
  beforeEach(() => {
    // Load fixture data
    // cy.fixture("adaptere").as("adaptereData");
    cy.visit("/adaptere");
    cy.reload();

    // Wait for Novari theme to be applied
    cy.get('[data-theme="novari"]', { timeout: 10000 }).should("exist");
  });

  it("should display breadcrumbs on all 3 pages", () => {
    cy.waitForAPI();

    // Check breadcrumbs on main page
    cy.contains("Adaptere").should("be.visible");
    cy.get("[data-cy='breadcrumbs']").should("be.visible");

    // Navigate to detail page
    cy.get("[data-cy='adapter-row']").first().click();

    // Check breadcrumbs on detail page
    cy.contains("Adapter Detaljer").should("be.visible");
    cy.get("[data-cy='breadcrumbs']").should("be.visible");

    // Navigate to component detail page
    cy.get("[data-cy='adapter-detail-table-row']").first().click();

    // Check breadcrumbs on component detail page
    cy.contains("Adapter Komponent").should("be.visible");
    cy.get("[data-cy='breadcrumbs']").should("be.visible");
  });

  it("should display adaptere table with data", () => {
    cy.contains("Adapter").should("be.visible");
    cy.contains("Oversikt over adaptere og deres status").should("be.visible");

    // Wait for API data to load
    // cy.waitForAPI();

    // Check if table headers are present
    cy.contains("Status").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Domene").should("be.visible");

    // Check if data is displayed
    cy.contains("fintlabs_no").should("be.visible");
    cy.contains("personvern").should("be.visible");
    cy.contains("utdanning").should("be.visible");
    cy.contains("vestfoldfylke_no").should("be.visible");
    cy.contains("helse").should("be.visible");
  });

  it("should filter data by organisation", () => {
    cy.waitForAPI();

    // Wait for initial data to be loaded and rendered (20 per page)
    cy.get("[data-cy='adapter-row']").should("have.length", 20);

    // Wait for filters to be interactive
    cy.get("#organisation-filter").should("be.visible").and("not.be.disabled");

    // Select organisation filter
    cy.get("#organisation-filter").select("fintlabs_no");

    // Check that only fintlabs_no data is shown (should be less than 20)
    cy.get("[data-cy='adapter-row']").should("have.length.lessThan", 20);
  });

  it("should filter data by domain", () => {
    cy.waitForAPI();

    // Wait for initial data to be loaded and rendered (20 per page)
    cy.get("[data-cy='adapter-row']").should("have.length", 20);

    // Wait for filters to be interactive
    cy.get("#domain-filter").should("be.visible").and("not.be.disabled");

    // Select domain filter
    cy.get("#domain-filter").select("personvern");

    // Check that only personvern data is shown
    cy.get("[data-cy='adapter-row']").should("have.length.at.least", 1);
  });

  it("should navigate, display details and display component modal", () => {
    cy.waitForAPI();

    // Click on first row
    cy.get("[data-cy='adapter-row']").first().click();
    cy.waitForAPI();

    // Should navigate to adapter detail page
    cy.url().should("include", "/adaptere/");
    cy.contains("Adapter Detaljer").should("be.visible");

    // Check adapter details
    cy.contains("Komponenter").should("be.visible");
    cy.contains("Adapter").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Domene").should("be.visible");
    cy.contains("Status").should("be.visible");

    // should navigate to component detail page
    cy.get("[data-cy='adapter-detail-table-row']").first().click();
    cy.waitForAPI();
    cy.url().should("include", "/adaptere/");
    cy.contains("Adapter Komponent").should("be.visible");

    // Check component details
    cy.contains("Driftspuls").should("be.visible");
    cy.contains("Delta overføring").should("be.visible");
    cy.contains("Full overføring").should("be.visible");

    // should display component modal with details
    cy.get("[data-cy='component-row']").first().click();
    cy.get("[data-cy='component-modal']").should("be.visible");
    cy.contains("Adapter Komponent").should("be.visible");
    cy.contains("Kapabiliteter").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Heartbeat Intervall").should("be.visible");
    cy.contains("Siste Heartbeat").should("be.visible");
    cy.contains("Siste Aktivitet").should("be.visible");
    cy.contains("Har Kontakt").should("be.visible");

    // should close component modal
    cy.get("[data-cy='component-modal']").should("be.visible");
    cy.get("[data-cy='component-modal-close']").click();
    cy.get("[data-cy='component-modal']").should("not.exist");
  });

  it("should paginate through adapters (20 per page)", () => {
    cy.waitForAPI();

    // Check first page has 20 items
    cy.get("[data-cy='adapter-row']").should("have.length", 20);

    // Check pagination controls are visible
    cy.get("[data-cy='pagination']").should("be.visible");

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Check second page has remaining items (25 total - 20 from first page = 5)
    cy.get("[data-cy='adapter-row']").should("have.length", 5);

    // Navigate back to page 1
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "1").click();
    });

    // Should be back at page 1 with 20 items
    cy.get("[data-cy='adapter-row']").should("have.length", 20);
  });

  it("should hide pagination when filtered results are less than 20", () => {
    cy.waitForAPI();

    // Initially pagination should be visible (25 total adapters)
    cy.get("[data-cy='pagination']").should("be.visible");

    // Filter to show only fintlabs_no organization
    cy.get("#organisation-filter").select("fintlabs_no");

    // Should show fewer results
    cy.get("[data-cy='adapter-row']").should("have.length.lessThan", 20);

    // Pagination should not be visible
    cy.get("[data-cy='pagination']").should("not.exist");
  });

  it("should reset to page 1 when filter is applied", () => {
    cy.waitForAPI();

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Verify we're on page 2
    cy.get("[data-cy='adapter-row']").should("have.length", 5);

    // Apply a filter
    cy.get("#domain-filter").select("utdanning");

    // Should reset to page 1 with filtered results
    cy.get("[data-cy='adapter-row']").should("have.length.at.least", 1);

    // Clear the filter by selecting "Alle domener"
    cy.get("#domain-filter").select("");

    // Should still be on page 1 with 20 items
    cy.get("[data-cy='adapter-row']").should("have.length", 20);
  });

  it("should reset to page 1 when sorting is applied", () => {
    cy.waitForAPI();

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Verify we're on page 2
    cy.get("[data-cy='adapter-row']").should("have.length", 5);

    // Click on a sortable column header
    cy.contains("th", "Organisasjon").click();

    // Should reset to page 1 with 20 items
    cy.get("[data-cy='adapter-row']").should("have.length", 20);
  });

  it("should maintain correct adapter count per page", () => {
    cy.waitForAPI();

    // Page 1: 20 items
    cy.get("[data-cy='adapter-row']").should("have.length", 20);

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Page 2: 5 items (25 total - 20 from first page)
    cy.get("[data-cy='adapter-row']").should("have.length", 5);
  });

  it("should navigate from any page to adapter detail", () => {
    cy.waitForAPI();

    // Navigate to page 2
    cy.get("[data-cy='pagination']").within(() => {
      cy.contains("button", "2").click();
    });

    // Wait for page 2 data to load
    cy.get("[data-cy='adapter-row']").should("have.length", 5);

    // Click on first row of page 2
    cy.get("[data-cy='adapter-row']").first().click();
    cy.waitForAPI();

    // Should navigate to adapter detail page
    cy.url().should("include", "/adaptere/");
    cy.contains("Adapter Detaljer").should("be.visible");
  });

  it("should clear all filters", () => {
    cy.waitForAPI();

    // Apply multiple filters
    cy.get("#organisation-filter").select("fintlabs_no");
    cy.get("#domain-filter").select("utdanning");

    // Wait for filters to apply
    cy.wait(300);

    // Should have filtered results
    cy.get("[data-cy='adapter-row']").should("have.length.at.least", 1);

    // Clear filters
    cy.contains("Tøm filtre").click();

    // Should show all data on page 1 (20 items)
    cy.get("[data-cy='adapter-row']").should("have.length", 20);

    // Verify checkboxes are checked
    cy.get('input[type="checkbox"]').should("be.checked");

    // Verify selects are reset
    cy.get("#organisation-filter").should("have.value", "");
    cy.get("#domain-filter").should("have.value", "");
  });
});
