import {
  MagnifyingGlassCheckmarkIcon,
  MenuElipsisVerticalIcon,
} from "@navikt/aksel-icons";
import { ActionMenu, Button } from "@navikt/ds-react";

interface SyncActionMenuOption {
  value: string;
  label: string;
}

interface SyncActionMenuProps {
  title: string;
  options: SyncActionMenuOption[];
  selectedValue?: string;
  clearLabel?: string;
  onSelect: (value: string) => void;
  onClear: () => void;
}

export function FilterActionMenu({
  title,
  options,
  selectedValue,
  clearLabel = "Vis alle",
  onSelect,
  onClear,
}: SyncActionMenuProps) {
  return (
    <ActionMenu>
      <ActionMenu.Trigger>
        <Button
          data-color="neutral"
          variant="tertiary"
          icon={
            selectedValue ? (
              <MagnifyingGlassCheckmarkIcon
                title={`Filter applied for ${title}`}
              />
            ) : (
              <MenuElipsisVerticalIcon title={`Filter for ${title}`} />
            )
          }
          size="small"
        />
      </ActionMenu.Trigger>
      <ActionMenu.Content>
        <ActionMenu.Group label={title}>
          <ActionMenu.Item onSelect={onClear}>{clearLabel}</ActionMenu.Item>
          <ActionMenu.Divider />
          {options.map((option) => (
            <ActionMenu.Item
              key={option.value}
              onSelect={() => onSelect(option.value)}
            >
              {option.label}
              {selectedValue === option.value ? " (valgt)" : ""}
            </ActionMenu.Item>
          ))}
        </ActionMenu.Group>
      </ActionMenu.Content>
    </ActionMenu>
  );
}
