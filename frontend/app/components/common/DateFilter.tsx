import { FunnelIcon } from "@navikt/aksel-icons";
import {
  Box,
  Button,
  DatePicker,
  HGrid,
  HStack,
  TextField,
  useRangeDatepicker,
} from "@navikt/ds-react";
import { useEffect, useState } from "react";
import {
  applyTimeToDate,
  createLast30DaysDisabledDays,
  formatTimeValue,
} from "~/utils/time";

interface DateFilterProps {
  dateRange: {
    from: Date | undefined;
    to: Date | undefined;
  };
  onApplyDateRange: (value: {
    from: Date | undefined;
    to: Date | undefined;
  }) => void;
  fromDate?: Date;
  toDate?: Date;
  compactActions?: boolean;
}

export function DateFilter({
  dateRange,
  onApplyDateRange,
  fromDate,
  toDate,
  compactActions = false,
}: DateFilterProps) {
  const [localDateRange, setLocalDateRange] = useState(dateRange);
  const [fromTime, setFromTime] = useState(formatTimeValue(dateRange.from));
  const [toTime, setToTime] = useState(formatTimeValue(dateRange.to));

  useEffect(() => {
    setLocalDateRange(dateRange);
    setFromTime(formatTimeValue(dateRange.from));
    setToTime(formatTimeValue(dateRange.to));
  }, [dateRange]);

  const disabledDays = createLast30DaysDisabledDays();
  const { datepickerProps, toInputProps, fromInputProps } = useRangeDatepicker({
    fromDate,
    toDate,
    onRangeChange: (value) => {
      setLocalDateRange({
        from: value?.from,
        to: value?.to,
      });
    },
    defaultSelected:
      localDateRange.from || localDateRange.to ? localDateRange : undefined,
    disabled: disabledDays,
  });

  const handleClearDates = () => {
    const clearedDateRange = { from: undefined, to: undefined };
    setLocalDateRange(clearedDateRange);
    setFromTime("");
    setToTime("");
    onApplyDateRange(clearedDateRange);
  };

  const handleApplyDates = () => {
    onApplyDateRange({
      from: applyTimeToDate(localDateRange.from, fromTime, false),
      to: applyTimeToDate(localDateRange.to, toTime, true),
    });
  };

  return (
    <Box
      padding="space-16"
      borderRadius="8"
      shadow="dialog"
      marginBlock="space-16"
    >
      <DatePicker
        key={`${localDateRange.from?.getTime() ?? "none"}-${localDateRange.to?.getTime() ?? "none"}`}
        {...datepickerProps}
      >
        <HGrid columns={4} gap="space-24">
          <DatePicker.Input {...fromInputProps} label="Fra dato" size="small" />
          <TextField
            label="Fra tid"
            size="small"
            type="time"
            value={fromTime}
            onChange={(event) => setFromTime(event.target.value)}
          />
          <DatePicker.Input {...toInputProps} label="Til dato" size="small" />
          <TextField
            label="Til tid"
            size="small"
            type="time"
            value={toTime}
            onChange={(event) => setToTime(event.target.value)}
          />
        </HGrid>
      </DatePicker>
      <HStack
        justify="space-between"
        marginBlock={compactActions ? "space-8" : "space-16"}
      >
        <Button
          size="small"
          variant="tertiary"
          icon={<FunnelIcon aria-hidden />}
          onClick={handleApplyDates}
        >
          Bruk datoer
        </Button>
        <Button variant="tertiary" size="small" onClick={handleClearDates}>
          Tøm datoer
        </Button>
      </HStack>
    </Box>
  );
}
