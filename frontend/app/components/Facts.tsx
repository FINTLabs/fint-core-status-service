export function Facts({ items }: { items: [string, React.ReactNode][] }) {
  return (
    <dl
      className="m-0 grid grid-cols-[max-content_1fr] gap-x-4 gap-y-1.5"
      style={{ fontSize: 14 }}
    >
      {items.map(([key, value]) => (
        <div key={key} className="contents">
          <dt className="text-[var(--ax-text-neutral-subtle)]">{key}</dt>
          <dd className="m-0 break-all">{value}</dd>
        </div>
      ))}
    </dl>
  );
}
