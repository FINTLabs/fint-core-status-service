export type Tone = "ok" | "bad" | "warn" | "muted" | "info";

const TONE_CLASS: Record<Tone, string> = {
  ok: "bg-[#06893A]",
  bad: "bg-[#C30000]",
  warn: "bg-[#C77300]",
  muted: "bg-[#B0B7BF]",
  info: "bg-[#0056B4]",
};

export function StatusDot({
  tone,
  children,
}: {
  tone: Tone;
  children?: React.ReactNode;
}) {
  return (
    <span className="inline-flex items-center gap-2 whitespace-nowrap">
      <span
        aria-hidden
        className={`inline-block h-2.5 w-2.5 shrink-0 rounded-full ${TONE_CLASS[tone]}`}
      />
      {children}
    </span>
  );
}
