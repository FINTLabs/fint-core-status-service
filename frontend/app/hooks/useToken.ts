import { AuthProperties } from "~/utils/auth";

/**
 * Hook to access the authorization token
 * This is a convenience wrapper around AuthProperties.getToken()
 *
 * Example usage in a component:
 * ```typescript
 * const token = useToken();
 * // Use token in your API calls
 * ```
 *
 * Note: You can also directly call AuthProperties.getToken() anywhere without a hook
 */
export function useToken(): string {
  return AuthProperties.getToken();
}
