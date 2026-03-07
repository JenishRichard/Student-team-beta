import axios from "axios";

const coreBaseUrl =
  import.meta.env.VITE_CORE_API_BASE_URL ?? import.meta.env.VITE_API_BASE_URL;

const coreClient = axios.create({
  baseURL: coreBaseUrl,
  timeout: 15000,
});

coreClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("access_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

coreClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem("access_token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default coreClient;
