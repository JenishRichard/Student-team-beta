import apiClient from "./apiClient";

export type LoginPayload = { username: string; password: string };

// Adjust this based on your backend response
export type LoginResponse = {
  token: string;
  user?: { id?: string; username?: string; role?: string };
};

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  // TODO: confirm endpoint path with backend (/auth/login, /login, /api/auth/login etc.)
  const { data } = await apiClient.post<LoginResponse>("/auth/login", payload);
  return data;
}