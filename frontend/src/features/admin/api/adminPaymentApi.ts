import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

/**
 * Fetch all payments for admin.
 */
export const fetchAdminPayments = async (page = 0, size = 10) => {
  const { data } = await api.get(API_ROUTES.admin.payments, {
    params: { page, size },
  });
  return unwrapApiResponse(data);
};

/**
 * Fetch a specific payment for admin.
 */
export const fetchAdminPayment = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.paymentById(id));
  return unwrapApiResponse(data);
};

/**
 * Fetch refunds for a specific payment.
 */
export const fetchPaymentRefunds = async (id, page = 0, size = 10) => {
  const { data } = await api.get(API_ROUTES.admin.paymentRefunds(id), {
    params: { page, size },
  });
  return unwrapApiResponse(data);
};

/**
 * Create a refund.
 * Requires Idempotency-Key.
 */
export const createRefund = async (id, payload) => {
  const { data } = await api.post(API_ROUTES.admin.paymentRefunds(id), payload, {
    headers: {
      'Idempotency-Key': crypto.randomUUID(),
    },
  });
  return unwrapApiResponse(data);
};

/**
 * Cancel a payment.
 * Requires Idempotency-Key.
 */
export const cancelPayment = async (id, payload) => {
  const { data } = await api.post(API_ROUTES.admin.cancelPayment(id), payload, {
    headers: {
      'Idempotency-Key': crypto.randomUUID(),
    },
  });
  return unwrapApiResponse(data);
};
