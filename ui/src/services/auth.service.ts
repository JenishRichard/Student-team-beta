import apiClient from "./appClient";
import type { AppRole } from "../types/roles";

export type LoginPayload = { email: string; password: string };

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
};

export type RegisterPayload = {
  userId?: string;
  email: string;
  password: string;
  roles?: AppRole[];
};

export type UserApi = {
  userId: string;
  email: string;
  role: AppRole;
  status: "ACTIVE" | "INVITED";
};

export type UpdateUserPayload = {
  email?: string;
  password?: string;
  role?: AppRole;
  status?: "ACTIVE" | "INVITED";
};

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>("/auth/login", payload);
  return data;
}

export async function register(payload: RegisterPayload): Promise<void> {
  await apiClient.post("/auth/register", payload);
}

export async function getUsers(): Promise<UserApi[]> {
  const { data } = await apiClient.get<UserApi[]>("/auth/users");
  return data;
}

export async function createUser(payload: RegisterPayload): Promise<UserApi> {
  const { data } = await apiClient.post<UserApi>("/auth/register", payload);
  return data;
}

export async function updateUser(userId: string, payload: UpdateUserPayload): Promise<UserApi> {
  const { data } = await apiClient.put<UserApi>(`/auth/users/${encodeURIComponent(userId)}`, payload);
  return data;
}

export async function deleteUser(userId: string): Promise<void> {
  await apiClient.delete(`/auth/users/${encodeURIComponent(userId)}`);
}
