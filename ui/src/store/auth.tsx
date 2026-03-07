import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { AppRole } from "../types/roles";

type User = { id?: string; username?: string; role?: AppRole } | null;

type AuthState = {
  token: string | null;
  user: User;
  isAuthenticated: boolean;
  isHydrated: boolean;
  setSession: (token: string, user: User) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User>(null);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    const savedToken = localStorage.getItem("access_token");
    const savedUser = localStorage.getItem("user");
    if (savedToken) setToken(savedToken);
    if (savedUser) setUser(JSON.parse(savedUser));
    setIsHydrated(true);
  }, []);

  const setSession = (t: string, u: User) => {
    localStorage.setItem("access_token", t);
    localStorage.setItem("user", JSON.stringify(u ?? null));
    setToken(t);
    setUser(u ?? null);
  };

  const logout = () => {
    localStorage.removeItem("access_token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ token, user, isAuthenticated: !!token, isHydrated, setSession, logout }),
    [token, user, isHydrated]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
