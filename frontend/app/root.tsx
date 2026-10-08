import {
  isRouteErrorResponse,
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from "react-router";
import { BodyShort, Heading } from "@navikt/ds-react";
import { ThemeProvider } from "novari-frontend-components";
import type { Route } from "./+types/root";
import akselHref from "@navikt/ds-css?url";
import themeHref from "./styles/novari-theme.css?url";
import tailwindHref from "./app.css?url";

export const links: Route.LinksFunction = () => [
  { rel: "stylesheet", href: tailwindHref },
  { rel: "stylesheet", href: themeHref },
  { rel: "stylesheet", href: akselHref },
];

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="nb">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>FINT Core Status</title>
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <Outlet />
    </ThemeProvider>
  );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  const status = isRouteErrorResponse(error) ? error.status : 500;
  const message = isRouteErrorResponse(error)
    ? (error.data?.message ?? error.statusText)
    : "Noe gikk galt.";
  return (
    <main className="mx-auto max-w-3xl p-8">
      <Heading size="large" level="1">
        {status === 404 ? "Fant ikke siden" : "Noe gikk galt"}
      </Heading>
      <BodyShort className="mt-2">{message}</BodyShort>
    </main>
  );
}
