import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

/**
 * Fetch a checkout quote (order summary before payment).
 */
export const fetchCheckoutQuote = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.checkoutQuote, payload);
  return data;
};

/**
 * Submit checkout for finalization.
 * Requires Idempotency-Key.
 */
export const submitCheckout = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.checkoutSubmit, payload, {
    headers: {
      'Idempotency-Key': crypto.randomUUID(),
    },
  });
  return data;
};

/**
 * Fetch a specific checkout session by ID.
 */
export const fetchCheckoutSession = async (id) => {
  const { data } = await api.get(API_ROUTES.customer.checkoutById(id));
  return data;
};

/**
 * Fetch my recent checkout sessions.
 */
export const fetchMyCheckoutSessions = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.customer.checkoutMe, { params });
  return data;
};
