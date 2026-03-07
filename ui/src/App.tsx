import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./store/auth";
import ProtectedRoute from "./routes/ProtectedRoute";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Rooms from "./pages/Rooms";
import Students from "./pages/Students";
import Teachers from "./pages/Teachers";
import Bookings from "./pages/Bookings";
import AdminUsers from "./pages/AdminUsers";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/rooms" element={<Rooms />} />
            <Route path="/admin/rooms" element={<Rooms />} />
            <Route path="/bookings" element={<Bookings />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["SUPER_ADMIN", "ADMIN"]} />}>
            <Route path="/students" element={<Students />} />
            <Route path="/teachers" element={<Teachers />} />
            <Route path="/admin-users" element={<AdminUsers />} />
            <Route path="/admin/users" element={<AdminUsers />} />
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
