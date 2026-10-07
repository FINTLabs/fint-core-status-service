// export function formatTimestamp(timestamp?: number | null): string {
//   if (!timestamp) {
//     return "Ikke tilgjengelig";
//   }
//
//   return new Date(timestamp).toLocaleString("no-NO", {
//     year: "numeric",
//     month: "2-digit",
//     day: "2-digit",
//     hour: "2-digit",
//     minute: "2-digit",
//     second: "2-digit",
//   });
// }

import { Tag } from "@navikt/ds-react";

export function formatJson(data: unknown): string {
  return JSON.stringify(data, null, 2);
}

export function OperationBadge({ operation }: { operation?: string | null }) {
  if (!operation) {
    return <span className="text-ax-neutral-600">Ikke tilgjengelig</span>;
  }

  const operationType = operation.toUpperCase() ?? "";

  const colorMap: Record<
    string,
    "neutral" | "info" | "success" | "warning" | "danger"
  > = {
    CREATE: "warning",
    UPDATE: "info",
    DELETE: "danger",
    VALIDATE: "success",
    UNKNOWN: "neutral",
  };

  const color = colorMap[operationType] ?? "neutral";
  return (
    <Tag variant="outline" data-color={color} size="xsmall">
      {operationType}
    </Tag>
  );
}

export function StatusBadge({
  failed,
  rejected,
  conflicted,
}: {
  failed: boolean;
  rejected: boolean;
  conflicted: boolean;
}) {
  const isSuccess = !failed && !rejected && !conflicted;
  const color = isSuccess ? "success" : "danger";

  return (
    <Tag variant="outline" data-color={color} size="xsmall">
      {failed
        ? "Failed"
        : rejected
          ? "Rejected"
          : conflicted
            ? "Conflicted"
            : "Success"}
    </Tag>
  );
}
