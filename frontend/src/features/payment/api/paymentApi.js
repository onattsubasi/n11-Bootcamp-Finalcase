import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

/**
 * Fetch customer payments.
 */
export const fetchPayments = async (page = 0, size = 10) => {
  const { data } = await api.get(API_ROUTES.customer.payments, {
    params: { page, size },
  });
  return data;
};

/**
 * Fetch a specific payment by ID.
 */
export const fetchPaymentById = async (id) => {
  const { data } = await api.get(API_ROUTES.customer.paymentById(id));
  return data;
};
