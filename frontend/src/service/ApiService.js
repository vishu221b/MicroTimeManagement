import axios from "axios";

const headers = {
  "content-type": "application/json",
};

const BASE_URL = "http://localhost:8080/mtm-dev/api/v1";

const TOKEN_STORAGE_KEYS = {
  ACCESS: "mtm_access_token",
  REFRESH: "mtm_refresh_token",
};

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
  axios
    .post(`${BASE_URL}/user/register`, data, { headers: headers })
    .then((response) => callback(response.data, null))
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const loginUser = async (data, callback) => {
  axios
    .post(`${BASE_URL}/auth/login`, data, { headers: headers })
    .then((response) => {
      const payload = response.data && response.data.data;
      if (payload && payload.accessToken) {
        localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS, payload.accessToken);
        localStorage.setItem(TOKEN_STORAGE_KEYS.REFRESH, payload.refreshToken);
      }
      callback(response.data, null);
    })
    .catch((err) => callback(null, toErrorPayload(err)));
};

export const logoutUser = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEYS.ACCESS);
  localStorage.removeItem(TOKEN_STORAGE_KEYS.REFRESH);
};

export const getStoredAccessToken = () =>
  localStorage.getItem(TOKEN_STORAGE_KEYS.ACCESS);
