import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const loginUser = async (credentials) => {
  const { data } = await api.post(API_ROUTES.auth.login, credentials);
  return data;
};

export const registerUser = async (payload) => {
  const { data } = await api.post(API_ROUTES.auth.register, payload);
  return data;
};

export const logoutUser = async () => {
  const { data } = await api.post(API_ROUTES.auth.logout, undefined, {
    skipAuthRefresh: true,
  });
  return data;
};

export const changePassword = async (payload) => {
  const { data } = await api.post(API_ROUTES.auth.changePassword, payload);
  return data;
};

export const fetchMe = async () => {
  const { data } = await api.get(API_ROUTES.auth.me);
  return data;
};

export const logoutAll = async () => {
  const { data } = await api.post(API_ROUTES.auth.logoutAll);
  return data;
};
