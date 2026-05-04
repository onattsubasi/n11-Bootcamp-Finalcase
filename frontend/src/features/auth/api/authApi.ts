import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const loginUser = async (credentials) => {
  const { data } = await api.post(API_ROUTES.auth.login, credentials);
  return unwrapApiResponse(data);
};

export const registerUser = async (payload) => {
  // Split fullName into firstName/lastName if missing, to match backend expectations
  if (payload.fullName && !payload.firstName && !payload.lastName) {
    const parts = payload.fullName.trim().split(/\s+/);
    payload.firstName = parts[0] || '';
    payload.lastName = parts.length > 1 ? parts.slice(1).join(' ') : '';
  }
  const { data } = await api.post(API_ROUTES.auth.register, payload);
  return unwrapApiResponse(data);
};

export const logoutUser = async () => {
  const { data } = await api.post(API_ROUTES.auth.logout, undefined, {
    skipAuthRefresh: true,
  });
  return unwrapApiResponse(data);
};

export const changePassword = async (payload) => {
  const { data } = await api.post(API_ROUTES.auth.changePassword, payload);
  return unwrapApiResponse(data);
};

export const fetchMe = async () => {
  const { data } = await api.get(API_ROUTES.auth.me);
  return unwrapApiResponse(data);
};

export const logoutAll = async () => {
  const { data } = await api.post(API_ROUTES.auth.logoutAll);
  return unwrapApiResponse(data);
};
