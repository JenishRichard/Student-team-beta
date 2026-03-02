import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute";
import { AuthProvider } from "./store/auth";

// Pages (create placeholders if not yet)
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Rooms from "./pages/Rooms";
import Students from "./pages/Students";
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
            <Route path="/students" element={<Students />} />
            <Route path="/bookings" element={<Bookings />} />
            <Route path="/admin-users" element={<AdminUsers />} />
          </Route>

          <Route path="*" element={<div>Not Found</div>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}