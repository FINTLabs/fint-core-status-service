import { BodyShort, Pagination } from "@navikt/ds-react";
import { PAGE_SIZE, useFilters } from "~/lib/filters";

export function Pager({ page, total }: { page: number; total: number }) {
  const { set } = useFilters();
  const pages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const from = total === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const to = Math.min(total, page * PAGE_SIZE);
  return (
    <div className="flex flex-wrap items-center justify-between gap-2 border-t border-[var(--ax-border-neutral-subtle)] px-3 py-2">
      <BodyShort size="small" className="text-[var(--ax-text-neutral-subtle)]">
        {total === 0 ? "Ingen rader" : `Viser ${from}–${to} av ${total}`}
      </BodyShort>
      {pages > 1 ? (
        <Pagination
          size="xsmall"
          page={page}
          count={pages}
          onPageChange={(next) => set({ side: String(next) }, true)}
          prevNextTexts
        />
      ) : null}
    </div>
  );
}
