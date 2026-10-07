import { Page } from "@navikt/ds-react";
import { NovariFooter } from "novari-frontend-components";

export function Footer() {
  const footerProps = {
    showThemeSwitcher: true,
    links: [
      { label: "Driftsmeldinger", href: "https://novari.no/driftsmeldinger/" },
      { label: "Opprett supportsak", href: "http://support.novari.no" },
      { label: "Brukerhjelp", href: "http://fintlabs.no" },
    ],
  };

  return (
    <Page.Block as="footer" width="2xl" gutters>
      <NovariFooter {...footerProps} />
    </Page.Block>
  );
}
