const STYLE: React.CSSProperties = {
  fontFamily: "ui-monospace, SFMono-Regular, Menlo, monospace",
  fontSize: 13,
};

export function Mono({ children }: { children: React.ReactNode }) {
  return <span style={STYLE}>{children}</span>;
}
