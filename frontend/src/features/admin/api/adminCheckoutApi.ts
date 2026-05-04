import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

/**
 * Fetch all checkout sessions for admin.
 */
export const fetchAdminCheckouts = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.admin.checkouts, { params });
  return unwrapApiResponse(data);
};

/**
 * Fetch a specific checkout session for admin.
 */
export const fetchAdminCheckout = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.checkoutById(id));
  return unwrapApiResponse(data);
};

/**
 * Retry checkout finalization.
 */
export const retryFinalization = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.retryFinalization(id));
  return unwrapApiResponse(data);
};

/**
 * Retry checkout compensation.
 */
export const retryCompensation = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.retryCompensation(id));
  return unwrapApiResponse(data);
};
