const TOKEN_STORAGE_KEYS = {
  ACCESS: "mtm_access_token",
  REFRESH: "mtm_refresh_token",
};

const listeners = new Set();

const notify = () => {
  listeners.forEach((listener) => {
    try {
      listener();
    } catch (e) {
      // listener failures must not break notification
    }
  });
};

export const getAccessToken = () =>
  localStorage.getItem(TOKEN_STORAGE_KEYS.ACCESS);

export const getRefreshToken = () =>
  localStorage.getItem(TOKEN_STORAGE_KEYS.REFRESH);

export const isAuthenticated = () => Boolean(getAccessToken());

export const setTokens = ({ accessToken, refreshToken }) => {
  if (accessToken) {
    localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS, accessToken);
  }
  if (refreshToken) {
    localStorage.setItem(TOKEN_STORAGE_KEYS.REFRESH, refreshToken);
  }
  notify();
};

export const clearTokens = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEYS.ACCESS);
  localStorage.removeItem(TOKEN_STORAGE_KEYS.REFRESH);
  notify();
};

export const subscribeAuth = (listener) => {
  listeners.add(listener);
  // Cross-tab sync: storage events fire in other tabs when localStorage changes.
  const onStorage = (event) => {
    if (
      event.key === TOKEN_STORAGE_KEYS.ACCESS ||
      event.key === TOKEN_STORAGE_KEYS.REFRESH
    ) {
      listener();
    }
  };
  window.addEventListener("storage", onStorage);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", onStorage);
  };
};
