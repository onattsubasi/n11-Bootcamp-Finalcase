import axios from 'axios';
import { useStore } from '../store';
import { API_ROUTES } from './routes';
import { normalizeAuthState } from '../features/auth/utils/normalizeAuthState';
import { queryClient } from '../lib/queryClient';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise = null;

const isAuthRoute = (url = '') => {
  const normalized = String(url);
  return (
    normalized.includes(API_ROUTES.auth.login) ||
    normalized.includes(API_ROUTES.auth.register) ||
    normalized.includes(API_ROUTES.auth.refresh) ||
    normalized.includes(API_ROUTES.auth.logout)
  );
};

const redirectToLogin = () => {
  if (typeof window === 'undefined') {
    return;
  }

  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login';
  }
};

const clearAuthenticatedState = () => {
  const { clearAuth } = useStore.getState();
  clearAuth();
  queryClient.clear();
};

api.interceptors.request.use((config) => {
  const token = useStore.getState().accessToken;

  if (token && !config.skipAuthHeader) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    if (originalRequest._retry || originalRequest.skipAuthRefresh || isAuthRoute(originalRequest.url)) {
      clearAuthenticatedState();
      redirectToLogin();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = api
          .post(API_ROUTES.auth.refresh, undefined, {
            skipAuthRefresh: true,
            skipAuthHeader: true,
          })
          .then((response) => {
            const authState = normalizeAuthState(response.data);
            if (!authState.accessToken) {
              throw new Error('Refresh response did not include access token');
            }

            useStore.getState().setAuth(authState);
            return authState.accessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const refreshedToken = await refreshPromise;
      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${refreshedToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      clearAuthenticatedState();
      redirectToLogin();
      return Promise.reject(refreshError);
    }
  }
);

export default api;
