import { useEffect } from "react";
import { useRevalidator } from "react-router";

export function useAutoRefresh(intervalMs: number) {
  const { revalidate } = useRevalidator();
  useEffect(() => {
    const timer = setInterval(() => {
      if (document.visibilityState === "visible") revalidate();
    }, intervalMs);
    return () => clearInterval(timer);
  }, [intervalMs, revalidate]);
}
