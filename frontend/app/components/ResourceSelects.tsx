import { Select } from "@navikt/ds-react";
import type { ModelDomain } from "~/api/types";
import { useFilters } from "~/lib/filters";

export function ResourceSelects({ model }: { model: ModelDomain[] }) {
  const { get, set } = useFilters();
  const domain = get("domene");
  const pkg = get("pakke");
  const packages = model.find((d) => d.name === domain)?.packages ?? [];
  const resources = packages.find((p) => p.name === pkg)?.resources ?? [];
  return (
    <>
      <Select
        size="small"
        label="Domene"
        value={domain}
        onChange={(e) =>
          set({ domene: e.target.value, pakke: null, ressurs: null })
        }
      >
        <option value="">Alle</option>
        {model.map((d) => (
          <option key={d.name} value={d.name}>
            {d.name}
          </option>
        ))}
      </Select>
      <Select
        size="small"
        label="Pakke"
        value={pkg}
        disabled={!domain}
        onChange={(e) => set({ pakke: e.target.value, ressurs: null })}
      >
        <option value="">Alle</option>
        {packages.map((p) => (
          <option key={p.name} value={p.name}>
            {p.name}
          </option>
        ))}
      </Select>
      <Select
        size="small"
        label="Ressurs"
        value={get("ressurs")}
        disabled={!pkg}
        onChange={(e) => set({ ressurs: e.target.value })}
      >
        <option value="">Alle</option>
        {resources.map((r) => (
          <option key={r} value={r}>
            {r}
          </option>
        ))}
      </Select>
    </>
  );
}
