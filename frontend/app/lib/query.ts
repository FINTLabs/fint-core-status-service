export function sharedFilterParams(search: URLSearchParams) {
  return {
    org: search.get("org"),
    corrId: search.get("corrid"),
    domain: search.get("domene"),
    package: search.get("pakke"),
    resource: search.get("ressurs"),
  };
}

export function toQueryString(
  params: Record<string, string | number | null | undefined>,
): string {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "")
      query.set(key, String(value));
  });
  return query.toString();
}
