import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../services/auth.service";
import { useAuth } from "../store/auth";
import { normalizeRole } from "../types/roles";

function getPrimaryRoleFromToken(token: string) {
  try {
    const payload = token.split(".")[1];
    if (!payload) return undefined;
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    const roles = decoded?.roles;
    if (Array.isArray(roles) && roles.length > 0 && typeof roles[0] === "string") {
      return normalizeRole(roles[0]);
    }
    return undefined;
  } catch {
    return undefined;
  }
}

export default function Login() {
  const nav = useNavigate();
  const { setSession } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!email.trim() || !password.trim()) {
      setError("Email and password are required");
      return;
    }

    try {
      setIsSubmitting(true);
      const normalizedEmail = email.trim().toLowerCase();
      const res = await login({ email: normalizedEmail, password });
      const role = getPrimaryRoleFromToken(res.accessToken);
      setSession(res.accessToken, { username: normalizedEmail, role });
      nav("/dashboard");
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? "Login failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="login-screen">
      <section className="login-card">
        <p className="login-kicker">Campus Rooms</p>
        <h1>Welcome Back</h1>
        <p className="login-subtitle">Sign in to manage rooms, bookings, and students.</p>

        <form className="login-form" onSubmit={onSubmit}>
          <label htmlFor="email">Email</label>
          <input
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="e.g. admin@tus.ie"
            autoComplete="email"
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            placeholder="Enter password"
            autoComplete="current-password"
          />

          {error ? <p className="form-error">{error}</p> : null}

          <button className="primary-btn" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </section>
    </main>
  );
}
