import { Button } from "@navikt/ds-react";
import { useEffect } from "react";
import { useFetcher, useRevalidator } from "react-router";
import type { CountResponse } from "~/api/types";

const POLL_MS = 30_000;

export function NewItemsBanner({
  countUrl,
  label,
}: {
  countUrl: string;
  label: string;
}) {
  const fetcher = useFetcher<CountResponse>();
  const revalidator = useRevalidator();
  const { load } = fetcher;

  useEffect(() => {
    const poll = () => {
      if (document.visibilityState === "visible") load(countUrl);
    };
    const timer = setInterval(poll, POLL_MS);
    return () => clearInterval(timer);
  }, [countUrl, load]);

  const count = fetcher.data?.count ?? 0;
  if (count === 0) return null;
  return (
    <Button
      size="small"
      variant="secondary"
      className="self-center"
      onClick={() => revalidator.revalidate()}
    >
      {count} {label}, vis
    </Button>
  );
}
