import axios from "axios";

const authBaseUrl =
  import.meta.env.VITE_AUTH_API_BASE_URL ?? import.meta.env.VITE_API_BASE_URL;

const apiClient = axios.create({
  baseURL: authBaseUrl,
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("access_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status;
    const requestUrl: string = err?.config?.url ?? "";
    const isAuthRequest =
      requestUrl.includes("/auth/login") || requestUrl.includes("/auth/register");

    if (status === 401 && !isAuthRequest) {
      localStorage.removeItem("access_token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default apiClient;
