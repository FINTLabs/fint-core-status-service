import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router";

export const PAGE_SIZE = 25;

export function pageFrom(params: URLSearchParams): number {
  return Math.max(1, Number(params.get("side") ?? "1") || 1);
}

export function useFilters() {
  const [params, setParams] = useSearchParams();

  const set = useCallback(
    (changes: Record<string, string | null>, keepPage = false) => {
      setParams(
        (current) => {
          const next = new URLSearchParams(current);
          Object.entries(changes).forEach(([key, value]) => {
            if (value === null || value === "") next.delete(key);
            else next.set(key, value);
          });
          if (!keepPage) next.delete("side");
          return next;
        },
        { preventScrollReset: true },
      );
    },
    [setParams],
  );

  return { params, set, get: (key: string) => params.get(key) ?? "" };
}

export function useDebouncedValue(
  value: string,
  onSettle: (value: string) => void,
  delay = 300,
) {
  const [draft, setDraft] = useState(value);
  const settle = useRef(onSettle);
  settle.current = onSettle;

  useEffect(() => setDraft(value), [value]);

  useEffect(() => {
    if (draft === value) return;
    const timer = setTimeout(() => settle.current(draft), delay);
    return () => clearTimeout(timer);
  }, [draft, value, delay]);

  return [draft, setDraft] as const;
}
