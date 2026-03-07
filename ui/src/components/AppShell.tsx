import { NavLink, useLocation } from "react-router-dom";
import { useAuth } from "../store/auth";
import { isAdminRole, roleLabel } from "../types/roles";

const navItems = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/rooms", label: "Rooms" },
  { to: "/bookings", label: "Bookings" },
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  const { pathname } = useLocation();
  const { user, logout } = useAuth();
  const isAdmin = isAdminRole(user?.role);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">Campus Rooms</div>
        <p className="brand-subtitle">Booking & Allocation</p>

        <nav className="sidebar-nav" aria-label="Primary">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `nav-item ${isActive || pathname.startsWith(item.to) ? "active" : ""}`
              }
            >
              {item.label}
            </NavLink>
          ))}
          {isAdmin ? (
            <>
              <NavLink
                to="/teachers"
                className={({ isActive }) =>
                  `nav-item ${isActive || pathname.startsWith("/teachers") ? "active" : ""}`
                }
              >
                Teachers
              </NavLink>
              <NavLink
                to="/students"
                className={({ isActive }) =>
                  `nav-item ${isActive || pathname.startsWith("/students") ? "active" : ""}`
                }
              >
                Students
              </NavLink>
              <NavLink
                to="/admin/users"
                className={({ isActive }) =>
                  `nav-item ${isActive || pathname.startsWith("/admin/users") ? "active" : ""}`
                }
              >
                Users
              </NavLink>
            </>
          ) : null}
        </nav>

        <div className="sidebar-footer">
          <p className="user-label">Signed in as</p>
          <p className="user-name">{user?.username ?? "Admin User"}</p>
          <p className="user-label">{roleLabel(user?.role)}</p>
          <button className="secondary-btn" onClick={logout} type="button">
            Log out
          </button>
        </div>
      </aside>

      <main className="content-area">{children}</main>
    </div>
  );
}
