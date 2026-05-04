import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios';

import { useStore } from '@/store';
import { API_ROUTES } from '@/api/routes';
import { normalizeAuthState } from '@/features/auth/utils/normalizeAuthState';
import { queryClient } from '@/lib/queryClient';

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
  skipAuthHeader?: boolean;
  skipAuthRefresh?: boolean;
};

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise: Promise<string> | null = null;

const isAuthRoute = (url = ''): boolean => {
  const normalized = String(url);
  return (
    normalized.includes(API_ROUTES.auth.login) ||
    normalized.includes(API_ROUTES.auth.register) ||
    normalized.includes(API_ROUTES.auth.refresh) ||
    normalized.includes(API_ROUTES.auth.logout)
  );
};

const redirectToLogin = (): void => {
  if (typeof globalThis.window === 'undefined') {
    return;
  }

  if (!globalThis.window.location.pathname.startsWith('/login')) {
    globalThis.window.location.href = '/login';
  }
};

const clearAuthenticatedState = (): void => {
  const { clearAuth } = useStore.getState();
  clearAuth();
  queryClient.clear();
};

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const requestConfig = config as RetryableRequestConfig;
  const token = useStore.getState().accessToken;

  if (token && !requestConfig.skipAuthHeader) {
    requestConfig.headers.Authorization = `Bearer ${token}`;
  }

  return requestConfig;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    if (
      originalRequest._retry ||
      originalRequest.skipAuthRefresh ||
      isAuthRoute(originalRequest.url)
    ) {
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
          } as AxiosRequestConfig)
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
      originalRequest.headers.Authorization = `Bearer ${refreshedToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      clearAuthenticatedState();
      redirectToLogin();
      return Promise.reject(refreshError);
    }
  },
);

export default api;
