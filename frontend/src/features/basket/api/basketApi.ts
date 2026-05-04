import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchBasket = async () => {
  const { data } = await api.get(API_ROUTES.customer.basket);
  return unwrapApiResponse(data);
};

export const addToBasket = async ({ productId, quantity }) => {
  const { data } = await api.post(API_ROUTES.customer.basketItems, { productId, quantity });
  return unwrapApiResponse(data);
};

export const updateBasketItem = async ({ itemId, quantity }) => {
  const { data } = await api.patch(API_ROUTES.customer.basketItemById(itemId), { quantity });
  return unwrapApiResponse(data);
};

export const removeBasketItem = async (itemId) => {
  const { data } = await api.delete(API_ROUTES.customer.basketItemById(itemId));
  return unwrapApiResponse(data);
};

export const clearBasket = async () => {
  const { data } = await api.delete(API_ROUTES.customer.basket);
  return unwrapApiResponse(data);
};
