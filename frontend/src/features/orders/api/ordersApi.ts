import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchOrders = async (page = 0, size = 10) => {
  const { data } = await api.get(API_ROUTES.customer.orders, {
    params: { page, size },
  });
  return unwrapApiResponse(data);
};

export const fetchOrderById = async (orderId) => {
  const { data } = await api.get(API_ROUTES.customer.orderById(orderId));
  return unwrapApiResponse(data);
};

export const cancelOrder = async (orderId, reason) => {
  const { data } = await api.post(API_ROUTES.customer.cancelOrder(orderId), {
    reason,
  });
  return unwrapApiResponse(data);
};

export const requestReturn = async (orderId, reason) => {
  const { data } = await api.post(API_ROUTES.customer.requestReturn(orderId), {
    reason,
  });
  return unwrapApiResponse(data);
};
