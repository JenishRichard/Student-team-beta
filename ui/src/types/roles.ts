export const APP_ROLES = ["SUPER_ADMIN", "ADMIN", "TEACHER", "STUDENT"] as const;

export type AppRole = (typeof APP_ROLES)[number];

export function normalizeRole(role?: string | null): AppRole | undefined {
  if (!role) return undefined;
  const normalized = role.trim().toUpperCase().replace(/\s+/g, "_");
  if (APP_ROLES.includes(normalized as AppRole)) {
    return normalized as AppRole;
  }
  return undefined;
}

export function isAdminRole(role?: string | null): boolean {
  const normalized = normalizeRole(role);
  return normalized === "SUPER_ADMIN" || normalized === "ADMIN";
}

export function roleLabel(role?: string | null): string {
  const normalized = normalizeRole(role);
  if (!normalized) return "Unknown";
  if (normalized === "SUPER_ADMIN") return "Super Admin";
  if (normalized === "ADMIN") return "Admin";
  if (normalized === "TEACHER") return "Teacher";
  return "Student";
}
