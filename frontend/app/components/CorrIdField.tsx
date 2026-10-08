import { TextField } from "@navikt/ds-react";
import { useDebouncedValue, useFilters } from "~/lib/filters";

export function CorrIdField() {
  const { get, set } = useFilters();
  const [value, setValue] = useDebouncedValue(get("corrid"), (next) =>
    set({ corrid: next.trim() }),
  );
  return (
    <TextField
      size="small"
      label="CorrId"
      placeholder="hele eller deler av corrId"
      className="min-w-64 flex-1 [&_input]:font-mono"
      value={value}
      onChange={(e) => setValue(e.target.value)}
    />
  );
}
