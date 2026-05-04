import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

/**
 * List coupons assigned to the current customer.
 */
export const fetchCustomerCoupons = async () => {
  const { data } = await api.get(API_ROUTES.customer.coupons);
  return unwrapApiResponse(data);
};
