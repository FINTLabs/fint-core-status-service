export function FilterBar({ children }: { children: React.ReactNode }) {
  return (
    <div className="sticky top-0 z-10 flex flex-col gap-2 rounded-lg border border-[var(--ax-border-neutral-subtle)] bg-[var(--ax-bg-default)] px-4 py-2.5 shadow-sm">
      {children}
    </div>
  );
}

export function FilterRow({ children }: { children: React.ReactNode }) {
  return <div className="flex flex-wrap items-end gap-2.5">{children}</div>;
}
