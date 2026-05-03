import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

/**
 * Fetch all checkout sessions for admin.
 */
export const fetchAdminCheckouts = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.admin.checkouts, { params });
  return data;
};

/**
 * Fetch a specific checkout session for admin.
 */
export const fetchAdminCheckout = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.checkoutById(id));
  return data;
};

/**
 * Retry checkout finalization.
 */
export const retryFinalization = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.retryFinalization(id));
  return data;
};

/**
 * Retry checkout compensation.
 */
export const retryCompensation = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.retryCompensation(id));
  return data;
};
