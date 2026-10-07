import { Box, Select } from "@navikt/ds-react";
import { type ChangeEvent, useState } from "react";
import { useSubmit } from "react-router";
import type { IUserSession } from "~/types";

export const EnvironmentSelector = ({ userSession, navigateTo }: { userSession: IUserSession; navigateTo: string }) => {
  const submit = useSubmit();

  const [envName, setEnvName] = useState(userSession.selectedEnv);

  const handleEnvChange = (event: ChangeEvent<HTMLSelectElement>) => {
    setEnvName(event.target.value as "beta" | "api" | "alpha");

    submit(
      {
        selectedEnv: event.target.value,
        actionType: "UPDATE_SELECTED_ENVIRONMENT",
        navigateTo: navigateTo ?? "/",
      },
      {
        method: "POST",
        navigate: false,
        // action: "/",
      }
    );
  };

  return (
    <Box className={"p-2"}>
      <Select size={"small"} label="Velg environment" hideLabel onChange={handleEnvChange} value={envName} className={"p-2"}>
        <option value="api">API</option>
        <option value="beta">BETA</option>
        <option value="alpha">ALPHA</option>
      </Select>
    </Box>
  );
};

// export function EnvironmentSelector() {
//   const [selectedEnvironment, setSelectedEnvironment] = useState<Environment>("API");
//
//   useEffect(() => {
//     const environment = getEnvironmentCookie();
//     setSelectedEnvironment(environment);
//
//     if (!document.cookie.includes("environment=")) {
//       setEnvironmentCookie("API");
//     }
//   }, []);
//
//   const handleEnvironmentChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
//     const newEnvironment = event.target.value as Environment;
//     setSelectedEnvironment(newEnvironment);
//     setEnvironmentCookie(newEnvironment);
//
//     window.dispatchEvent(
//       new CustomEvent("environmentChanged", {
//         detail: { environment: newEnvironment },
//       })
//     );
//   };
//
//   return (
//     <Select
//       value={selectedEnvironment}
//       onChange={handleEnvironmentChange}
//       size="small"
//       label="Environment"
//       hideLabel
//     >
//       <option value="API">API</option>
//       <option value="BETA">BETA</option>
//       <option value="ALPHA">ALPHA</option>
//     </Select>
//   );
// }
