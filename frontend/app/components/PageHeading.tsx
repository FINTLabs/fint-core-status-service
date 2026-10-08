import { BodyShort, Heading } from "@navikt/ds-react";

export function PageHeading({
  title,
  detail,
}: {
  title: string;
  detail?: React.ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-baseline justify-between gap-2">
      <Heading size="medium" level="1">
        {title}
      </Heading>
      {detail ? (
        <BodyShort
          size="small"
          className="text-[var(--ax-text-neutral-subtle)]"
        >
          {detail}
        </BodyShort>
      ) : null}
    </div>
  );
}
