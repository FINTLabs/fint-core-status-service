import {
  type ActionFunctionArgs,
  data,
  isRouteErrorResponse,
  Links,
  type LoaderFunctionArgs,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
  useLoaderData,
  useLocation,
  useNavigate,
} from "react-router";
import { Box, Page } from "@navikt/ds-react";
import { Footer } from "./components/layout/Footer";

import type { Route } from "./+types/root";
import akselHref from "@navikt/ds-css?url";
import themeHref from "./styles/novari-theme.css?url";
import tailwindHref from "./app.css?url";
import { selectedEnvCookie } from "~/utils/cookies";
import { AuthProperties } from "~/utils/auth";
import type { IUserSession } from "~/types";
import { NovariHeader, ThemeProvider } from "novari-frontend-components";
import { EnvironmentSelector } from "~/components/common/EnvironmentSelector";

//TODO: fix MSW for local development

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let server: any;

async function initializeMSW() {
  try {
    const enableMocking = import.meta.env.VITE_MOCK_CYPRESS === "true";

    if (enableMocking) {
      if (typeof window !== "undefined") {
        const { worker } = await import("../cypress/mocks/browser");
        await worker.start({
          serviceWorker: {
            url: "/mockServiceWorker.js",
          },
          onUnhandledRequest: "warn",
        });
        // console.log("[MSW] Worker started with handlers:", worker.listHandlers().length);
        // MSW worker started successfully
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (window as any).__mswReady = true;
      } else {
        const { server: nodeServer } = await import("../cypress/mocks/node");
        server = nodeServer;
        server.listen({ onUnhandledRequest: "bypass" });
        // MSW server started successfully
      }
    } else {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      if (typeof window !== "undefined") (window as any).__mswReady = true;
    }
  } catch {
    // MSW initialization failed - handle silently
    // console.warn("MSW initialization failed:", error);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if (typeof window !== "undefined") (window as any).__mswReady = true;
  }
}

// Initialize MSW
initializeMSW();

export const links: Route.LinksFunction = () => [
  { rel: "stylesheet", href: tailwindHref, as: "style" }, // Tailwind utilities
  { rel: "stylesheet", href: themeHref, as: "style" }, // novari-theme.css
  { rel: "stylesheet", href: akselHref, as: "style" }, // Aksel last (takes precedence)
];

export const loader = async ({ request }: LoaderFunctionArgs) => {
  AuthProperties.setProperties(request);

  const cookieHeader = request.headers.get("Cookie");
  const cookieValue = await selectedEnvCookie.parse(cookieHeader);

  let selectedEnv = cookieValue;
  if (!selectedEnv) {
    selectedEnv = "api";
  }

  const userSession: IUserSession = {
    selectedEnv: selectedEnv as "beta" | "api" | "alpha",
  };

  if (!cookieValue) {
    const newCookieHeader = await selectedEnvCookie.serialize(selectedEnv);
    return data(
      { userSession },
      {
        headers: {
          "Set-Cookie": newCookieHeader,
        },
      },
    );
  }

  return new Response(JSON.stringify({ userSession }), {
    headers: { "Content-Type": "application/json" },
  });
};

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
        <title>Status</title>
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
  const { userSession } = useLoaderData<{
    userSession: IUserSession;
  }>();
  const location = useLocation();
  const navigate = useNavigate();
  // const theme: "light" | "dark" = "light";

  return (
    <ThemeProvider>
      <Page
        footer={
          <Box padding="space-4" as="footer" className={"novari-footer"}>
            <Page.Block>
              <Footer />
            </Page.Block>
          </Box>
        }
      >
        <Page.Block as="header">
          {/*<Theme theme="light">*/}
          <Box as="nav" data-cy="novari-header" shadow="dialog">
            <NovariHeader
              appName="Fint Core Status Service"
              menu={[
                {
                  label: "Dashboard",
                  action: "/",
                },
                {
                  label: "Adaptere",
                  action: "/adaptere",
                },
                {
                  label: "Hendelser",
                  action: "/hendelser",
                },
                {
                  label: "Synkronisering",
                  action: "/sync",
                },
              ]}
              isLoggedIn={true}
              onLogout={() => {}}
              onLogin={() => {}}
              onMenuClick={(action) => navigate(action)}
              displayName={""}
              className={"novari-header"}
              showLogoWithTitle={true}
            >
              <EnvironmentSelector
                userSession={userSession}
                navigateTo={location.pathname}
              />
            </NovariHeader>
          </Box>
          {/*</Theme>*/}
        </Page.Block>

        <Box padding="space-32" paddingBlock="space-8" as="main">
          <Page.Block gutters width="2xl">
            <Outlet context={userSession} />
          </Page.Block>
        </Box>
      </Page>
    </ThemeProvider>
  );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  let message = "Oops!";
  let details = "An unexpected error occurred.";

  if (isRouteErrorResponse(error)) {
    message = error.status === 404 ? "404" : "Error";
    details =
      error.status === 404
        ? "The requested page could not be found."
        : error.statusText || details;
  } else if (import.meta.env.DEV && error && error instanceof Error) {
    details = error.message;
  }

  return (
    <Page>
      {/*<Header />*/}
      <Page.Block as="main" width="xl" gutters>
        <Box className="py-8">
          <h1 className="text-3xl font-ax-bold mb-4">{message}</h1>
          <p className="text-lg mb-4">{details}</p>
        </Box>
      </Page.Block>
      <Footer />
    </Page>
  );
}

export async function action({ request }: ActionFunctionArgs) {
  const formData = await request.formData();
  const actionType = formData.get("actionType") as string;

  if (actionType === "UPDATE_SELECTED_ENVIRONMENT") {
    const selectedEnv = formData.get("selectedEnv") as string;
    const navigateTo = (formData.get("navigateTo") as string) || "/";

    const newCookieHeader = await selectedEnvCookie.serialize(selectedEnv);

    // Redirect to the specified path after setting the cookie
    return new Response(null, {
      status: 302,
      headers: {
        "Set-Cookie": newCookieHeader,
        Location: navigateTo,
      },
    });
  }

  return new Response(JSON.stringify({ ok: true }), {
    headers: { "Content-Type": "application/json" },
  });
}
