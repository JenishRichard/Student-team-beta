import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../store/auth";
import AppShell from "../components/AppShell";
import type { AppRole } from "../types/roles";
import { normalizeRole } from "../types/roles";

type ProtectedRouteProps = {
  allowedRoles?: AppRole[];
};

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, isHydrated, user } = useAuth();
  if (!isHydrated) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRoles && allowedRoles.length > 0) {
    const role = normalizeRole(user?.role);
    if (!role || !allowedRoles.includes(role)) {
      return <Navigate to="/dashboard" replace />;
    }
  }

  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}
