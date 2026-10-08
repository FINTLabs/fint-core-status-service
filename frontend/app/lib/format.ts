const timeFormat = new Intl.DateTimeFormat("nb-NO", {
  hour: "2-digit",
  minute: "2-digit",
  timeZone: "Europe/Oslo",
});
const dateFormat = new Intl.DateTimeFormat("nb-NO", {
  day: "2-digit",
  month: "2-digit",
  timeZone: "Europe/Oslo",
});
const numberFormat = new Intl.NumberFormat("nb-NO");

export function formatNumber(value: number): string {
  return numberFormat.format(value);
}

export function formatAgo(
  iso: string | null | undefined,
  now: Date = new Date(),
): string {
  if (!iso) return "aldri";
  const seconds = Math.max(
    0,
    Math.round((now.getTime() - new Date(iso).getTime()) / 1000),
  );
  if (seconds < 60) return `${seconds} s siden`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} min siden`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} t ${minutes % 60} min siden`;
  return `${Math.floor(hours / 24)} d siden`;
}

export function formatDuration(
  fromIso: string,
  toIso: string | null | undefined,
  now: Date = new Date(),
): string {
  const end = toIso ? new Date(toIso) : now;
  const seconds = Math.max(
    0,
    Math.round((end.getTime() - new Date(fromIso).getTime()) / 1000),
  );
  if (seconds < 60) return `${seconds} s`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} min ${seconds % 60} s`;
  return `${Math.floor(minutes / 60)} t ${minutes % 60} min`;
}

export function formatDateTime(
  iso: string | null | undefined,
  now: Date = new Date(),
): string {
  if (!iso) return "–";
  const date = new Date(iso);
  const day = dateFormat.format(date);
  const today = dateFormat.format(now);
  const yesterday = dateFormat.format(new Date(now.getTime() - 86_400_000));
  const prefix = day === today ? "i dag" : day === yesterday ? "i går" : day;
  return `${prefix} ${timeFormat.format(date)}`;
}

export function minutesSince(
  iso: string | null | undefined,
  now: Date = new Date(),
): number {
  return iso
    ? Math.floor((now.getTime() - new Date(iso).getTime()) / 60_000)
    : 0;
}

export function resourcePath(
  domain?: string | null,
  pkg?: string | null,
  resource?: string | null,
): string {
  return [domain, pkg, resource].filter(Boolean).join("/") || "–";
}
