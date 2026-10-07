import { HStack, Tooltip } from "@navikt/ds-react";
import { CalendarIcon } from "@navikt/aksel-icons";

export const formatTimeValue = (value: Date | undefined): string => {
  if (!value) {
    return "";
  }

  const hours = String(value.getHours()).padStart(2, "0");
  const minutes = String(value.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
};

export const applyTimeToDate = (
  value: Date | undefined,
  time: string,
  endOfDayWhenMissing: boolean,
): Date | undefined => {
  if (!value) {
    return undefined;
  }

  const next = new Date(value);
  if (time) {
    const [hours, minutes] = time.split(":");
    const parsedHours = Number(hours);
    const parsedMinutes = Number(minutes);

    if (!Number.isNaN(parsedHours) && !Number.isNaN(parsedMinutes)) {
      next.setHours(parsedHours, parsedMinutes, 0, 0);
      return next;
    }
  }

  if (endOfDayWhenMissing) {
    next.setHours(23, 59, 59, 999);
  } else {
    next.setHours(0, 0, 0, 0);
  }
  return next;
};

export const createLast30DaysDisabledDays = (now = new Date()) => {
  const thirtyDaysAgo = new Date(now);
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
  thirtyDaysAgo.setHours(0, 0, 0, 0);

  const today = new Date(now);
  today.setHours(23, 59, 59, 999);

  return [{ before: thirtyDaysAgo }, { after: today }];
};

export const formatTimestampDetailed = (
  value?: number | null,
): React.ReactNode => {
  if (!value) return "Aldri";

  const date = new Date(value);

  const formatted = date.toLocaleString("nb-NO", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  // const detailed = date.toLocaleString("nb-NO", {
  //   year: "numeric",
  //   month: "long",
  //   day: "numeric",
  //   hour: "2-digit",
  //   minute: "2-digit",
  //   second: "2-digit",
  //   timeZoneName: "short",
  // });

  return (
    <HStack gap={"space-2"}>
      <span>{formatted}</span>
      <Tooltip content={value.toString()}>
        <CalendarIcon fontSize="1.5rem" />
      </Tooltip>
    </HStack>
  );
};

export const formatDateRelative = (value?: number | null): string => {
  if (!value || value === 0) return "-";

  const date = new Date(value);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const dateOnly = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
  );

  // Calculate difference in days
  const diffTime = today.getTime() - dateOnly.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

  // Today
  if (diffDays === 0) {
    return "I dag";
  }

  // Yesterday
  if (diffDays === 1) {
    return "I går";
  }

  // Last 6 days (2-6 days ago) - show day of week
  if (diffDays >= 2 && diffDays <= 6) {
    const weekdays = [
      "Søndag",
      "Mandag",
      "Tirsdag",
      "Onsdag",
      "Torsdag",
      "Fredag",
      "Lørdag",
    ];
    return weekdays[date.getDay()];
  }

  // Within the last year - show day + month
  const oneYearAgo = new Date(
    now.getFullYear() - 1,
    now.getMonth(),
    now.getDate(),
  );
  if (date >= oneYearAgo) {
    const months = [
      "januar",
      "februar",
      "mars",
      "april",
      "mai",
      "juni",
      "juli",
      "august",
      "september",
      "oktober",
      "november",
      "desember",
    ];
    return `${date.getDate()}. ${months[date.getMonth()]}`;
  }

  // Older than a year - full date (DD.MM.YYYY)
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${day}.${month}.${year}`;
};
