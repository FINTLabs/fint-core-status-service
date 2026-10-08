import { Select } from "@navikt/ds-react";
import type { OrgNode } from "~/api/types";
import { useFilters } from "~/lib/filters";

export function OrgSelect({ orgs }: { orgs: OrgNode[] }) {
  const { get, set } = useFilters();
  return (
    <Select
      size="small"
      label="Org"
      value={get("org")}
      onChange={(e) => set({ org: e.target.value })}
    >
      <option value="">Alle org</option>
      {orgs.flatMap((org) => [
        <option key={org.orgId} value={org.orgId}>
          {org.orgId}
        </option>,
        ...org.subOrgs.map((sub) => (
          <option key={sub} value={sub}>
            {"  "}
            {sub}
          </option>
        )),
      ])}
    </Select>
  );
}
