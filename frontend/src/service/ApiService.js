import axios from "axios";
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from "./AuthStorage";

const BASE_URL = "http://localhost:8080/mtm-dev/api/v1";

const PUBLIC_PATHS = ["/auth/login", "/auth/refresh", "/user/register"];

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { "content-type": "application/json" },
});

const isPublicPath = (url = "") => PUBLIC_PATHS.some((p) => url.endsWith(p));

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token && !isPublicPath(config.url)) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let pendingRefresh = null;

const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("No refresh token available");
  }
  if (!pendingRefresh) {
    pendingRefresh = axios
      .post(
        `${BASE_URL}/auth/refresh`,
        { token: refreshToken },
        { headers: { "content-type": "application/json" } }
      )
      .then((response) => {
        const payload = response.data && response.data.data;
        if (!payload || !payload.accessToken) {
          throw new Error("Refresh response missing access token");
        }
        setTokens({
          accessToken: payload.accessToken,
          refreshToken: payload.refreshToken,
        });
        return payload.accessToken;
      })
      .finally(() => {
        pendingRefresh = null;
      });
  }
  return pendingRefresh;
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response && error.response.status;

    if (
      status === 401 &&
      original &&
      !original._retried &&
      !isPublicPath(original.url) &&
      getRefreshToken()
    ) {
      original._retried = true;
      try {
        const newAccessToken = await refreshAccessToken();
        original.headers = original.headers || {};
        original.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(original);
      } catch (refreshError) {
        clearTokens();
        if (typeof window !== "undefined") {
          window.location.assign("/login");
        }
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

const toErrorPayload = (err) => {
  if (err.name === "AxiosError" && err.code === "ERR_NETWORK") {
    return {
      error: {
        message: err.message + ": Please check your internet connection.",
      },
    };
  }
  return err.response ? err.response.data : { error: { message: err.message } };
};

export const registerUser = async (data, callback) => {
  apiClient
    .post(`/user/register`, data)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const loginUser = async (data, callback) => {
  apiClient
    .post(`/auth/login`, data)
    .then((response) => {
      const payload = response.data && response.data.data;
      if (payload && payload.accessToken) {
        setTokens({
          accessToken: payload.accessToken,
          refreshToken: payload.refreshToken,
        });
      }
      callback(response.data, null);
    })
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const logoutUser = async () => {
  try {
    if (getAccessToken()) {
      await apiClient.post(`/auth/logout`, {});
    }
  } catch (e) {
    // Server-side logout failure shouldn't keep the user signed in locally.
  } finally {
    clearTokens();
  }
};

// --- Activity API ---

export const createActivity = async (payload, callback) => {
  apiClient
    .post(`/activity`, payload)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const getActivitiesForDate = async (date, callback) => {
  apiClient
    .get(`/activity/getAllForDate`, { params: { date } })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const updateActivity = async (date, payload, callback) => {
  apiClient
    .put(`/activity`, payload, { params: { date } })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const deleteActivity = async (date, recordId, callback) => {
  apiClient
    .delete(`/activity`, { params: { date, recordId } })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

// --- User profile API ---

export const getUserProfile = async (callback) => {
  apiClient
    .get(`/user/profile`)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const updateUserDetails = async (payload, callback) => {
  apiClient
    .put(`/user/update`, payload)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const changeUserPassword = async (payload, callback) => {
  apiClient
    .post(`/user/resetPassword`, payload)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

// --- Role admin API ---

export const listRoles = async (callback, page = 0, size = 50) => {
  apiClient
    .get(`/role`, { params: { page, size } })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const createRole = async (payload, callback) => {
  apiClient
    .post(`/role`, payload)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const updateRole = async (payload, callback) => {
  apiClient
    .put(`/role`, payload)
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const deleteRole = async (roleId, callback) => {
  apiClient
    .delete(`/role`, { params: { roleId } })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export { getAccessToken, isAuthenticated } from "./AuthStorage";
export default apiClient;
