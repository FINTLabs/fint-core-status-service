export type EnvKey = "api" | "beta" | "alpha";

export interface Environment {
  key: EnvKey;
  slug: string;
  label: string;
}

export const ENVIRONMENTS: Environment[] = [
  { key: "api", slug: "prod", label: "api" },
  { key: "beta", slug: "beta", label: "beta" },
  { key: "alpha", slug: "alpha", label: "alpha" },
];

export function environmentBySlug(
  slug: string | undefined,
): Environment | undefined {
  return ENVIRONMENTS.find((env) => env.slug === slug);
}
