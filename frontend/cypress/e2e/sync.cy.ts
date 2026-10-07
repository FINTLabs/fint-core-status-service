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

describe("Sync Page", () => {
  beforeEach(() => {
    // Load fixture data
    // cy.fixture("sync").as("syncData");
    cy.visit("/sync");

    // Wait for Novari theme to be applied
    cy.get('[data-theme="novari"]', { timeout: 10000 }).should("exist");
    cy.wait(1000);
  });

  it("should display sync table with data", () => {
    cy.contains("Synkronisering").should("be.visible");
    cy.contains("Oversikt over synkroniseringer").should("be.visible");

    // Wait for API data to load
    cy.waitForAPI();

    // Check if table headers are present
    cy.contains("Status").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Domene").should("be.visible");
    cy.contains("Pakke").should("be.visible");
    cy.contains("Ressurs").should("be.visible");
    cy.contains("Type").should("be.visible");
    cy.contains("Entiteter").should("be.visible");
    cy.contains("Sider").should("be.visible");
    cy.contains("Fremdrift").should("be.visible");

    // Check if data is displayed (should have 5 sync items)
    cy.get("[data-cy='sync-row']").should("have.length", 5);
  });

  it("should filter data by sync type", () => {
    cy.waitForAPI();

    // Initially all 5 items visible
    cy.get("[data-cy='sync-row']").should("have.length", 5);

    // Uncheck FULL
    cy.contains("label", "Full").click();

    // Should show only DELTA syncs (2 items)
    cy.get("[data-cy='sync-row']").should("have.length", 2);

    // Check FULL again and uncheck DELTA
    cy.contains("label", "Full").click();
    cy.contains("label", "Delta").click();

    // Should show only FULL syncs (3 items)
    cy.get("[data-cy='sync-row']").should("have.length", 3);
  });

  it("should filter data by status", () => {
    cy.waitForAPI();

    // Initially all 5 items visible
    cy.get("[data-cy='sync-row']").should("have.length", 5);

    // Uncheck "Fullført"
    cy.contains("label", "Fullført").click();

    // Should show only ongoing syncs (1 item)
    cy.get("[data-cy='sync-row']").should("have.length", 1);

    // Check "Fullført" again and uncheck "Pågår"
    cy.contains("label", "Fullført").click();
    cy.contains("label", "Pågår").click();

    // Should show only finished syncs (4 items)
    cy.get("[data-cy='sync-row']").should("have.length", 4);
  });

  it("should filter data by organisation", () => {
    cy.waitForAPI();

    // Wait for filters to be interactive
    cy.get("#organisation-filter").should("be.visible").and("not.be.disabled");

    // Select organisation filter
    cy.get("#organisation-filter").select("agderfk.no");

    // Should show only agderfk.no data (1 item)
    cy.get("[data-cy='sync-row']").should("have.length", 1);
  });

  it("should filter data by domain", () => {
    cy.waitForAPI();

    // Wait for filters to be interactive
    cy.get("#domain-filter").should("be.visible").and("not.be.disabled");

    // Select domain filter
    cy.get("#domain-filter").select("utdanning");

    // Should show only utdanning domain data (2 items)
    cy.get("[data-cy='sync-row']").should("have.length", 2);
  });

  it("should clear all filters", () => {
    cy.waitForAPI();

    // Apply multiple filters - uncheck Full to show only Delta
    cy.contains("label", "Full").click();

    // Wait for filter to apply
    cy.wait(300);

    // Should show only DELTA syncs (2 items)
    cy.get("[data-cy='sync-row']").should("have.length", 2);

    // Add organization filter
    cy.get("#organisation-filter").select("fintlabs.no");

    // Wait for filter to apply
    cy.wait(300);

    // Should have filtered results (fintlabs.no has 1 DELTA sync)
    cy.get("[data-cy='sync-row']").should("have.length", 1);

    // Clear filters
    cy.contains("Tøm filtre").click();

    // Should show all data again
    cy.get("[data-cy='sync-row']").should("have.length", 5);

    // Verify all checkboxes are checked
    cy.get('input[value="full"]').should("be.checked");
    cy.get('input[value="delta"]').should("be.checked");
    cy.get('input[value="finished"]').should("be.checked");
    cy.get('input[value="ongoing"]').should("be.checked");

    // Verify selects are reset
    cy.get("#organisation-filter").should("have.value", "");
    cy.get("#domain-filter").should("have.value", "");
  });

  it("should display progress bars correctly", () => {
    cy.waitForAPI();

    // Check that progress bars exist
    cy.get("[data-cy='sync-row']").should("have.length", 5);

    // Progress bars should be visible
    cy.get("[data-cy='sync-row']")
      .first()
      .within(() => {
        // Check for Aksel ProgressBar component
        cy.get("div[role='progressbar']").should("exist");
        cy.get("div[role='progressbar']").should("have.attr", "aria-valuenow");
        cy.get("div[role='progressbar']").should("have.attr", "aria-valuemax", "100");
      });

    // Check that percentage text is displayed
    cy.get("[data-cy='sync-row']").first().contains(/\d+%/).should("be.visible");
  });

  it("should show correct sync type badges", () => {
    cy.waitForAPI();

    // Check for FULL badge
    cy.contains(".bg-ax-accent-200", "FULL").should("be.visible");

    // Check for DELTA badge
    cy.contains(".bg-ax-meta-purple-200", "DELTA").should("be.visible");
  });

  it("should display finished and ongoing status correctly", () => {
    cy.waitForAPI();

    // Filter to show only ongoing
    cy.contains("label", "Fullført").click();

    cy.get("[data-cy='sync-row']").should("have.length", 1);

    // Check for yellow indicator (ongoing)
    cy.get(".bg-yellow-100").should("exist");
    cy.get(".text-yellow-600").should("exist");

    // Reset and show only finished
    cy.contains("label", "Fullført").click();
    cy.contains("label", "Pågår").click();

    // Check for green indicators (finished)
    cy.get(".bg-ax-success-200").should("exist");
    cy.get(".text-ax-success-700").should("exist");
  });

  it("should open modal when row is clicked", () => {
    cy.waitForAPI();

    // Click on first row
    cy.get("[data-cy='sync-row']").first().click();

    // Modal should open
    cy.get("[data-cy='sync-modal']").should("be.visible");
    cy.contains("Synkronisering Detaljer").should("be.visible");
    cy.contains("Oversikt").should("be.visible");
    cy.contains("Correlation ID").should("be.visible");
  });

  it("should display correlation ID and page details in modal", () => {
    cy.waitForAPI();

    // Click on first row to open modal
    cy.get("[data-cy='sync-row']").first().click();

    // Check overview section
    cy.contains("Correlation ID").should("be.visible");
    cy.contains("c8491980-05ea-467b-bcdb-a381cb1be122").should("be.visible");
    cy.contains("Adapter ID").should("be.visible");
    cy.contains("Organisasjon").should("be.visible");
    cy.contains("Domene").should("be.visible");
    cy.contains("Pakke").should("be.visible");
    cy.contains("Ressurs").should("be.visible");

    // Check pages section
    cy.contains("Sider (1)").should("be.visible");
    cy.contains("Side Nr.").should("be.visible");
    cy.contains("Størrelse").should("be.visible");
    cy.contains("Tidspunkt").should("be.visible");

    // Check page data is displayed
    cy.contains("15 entiteter").should("be.visible");
  });

  it("should close modal when close button is clicked", () => {
    cy.waitForAPI();

    // Open modal
    cy.get("[data-cy='sync-row']").first().click();
    cy.get("[data-cy='sync-modal']").should("be.visible");

    // Close modal
    cy.get("[data-cy='sync-modal-close']").click();

    // Modal should be closed
    cy.get("[data-cy='sync-modal']").should("not.exist");
  });

  it("should show multiple pages in modal for multi-page sync", () => {
    cy.waitForAPI();

    // Click on second row (which has 3 pages)
    cy.get("[data-cy='sync-row']").eq(1).click();

    // Modal should show pages section with 3 pages
    cy.contains("Sider (3)").should("be.visible");

    // Should have 3 rows in the pages table (plus header)
    cy.get("[data-cy='sync-modal']").within(() => {
      cy.get("table tbody tr").should("have.length", 3);
    });
  });

  it("should display progress information in modal", () => {
    cy.waitForAPI();

    // Click on first row
    cy.get("[data-cy='sync-row']").first().click();

    // Check progress information
    cy.contains("Fremdrift").should("be.visible");
    cy.contains("15 / 15 entiteter").should("be.visible");
    cy.contains("1 / 1 sider").should("be.visible");
    cy.contains("Varighet").should("be.visible");
  });
});
