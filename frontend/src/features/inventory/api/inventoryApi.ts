import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchInventoryItems = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryItems, { params });
  return unwrapApiResponse(data);
};

export const fetchInventoryByProductId = async (productId) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryByProductId(productId));
  return unwrapApiResponse(data);
};

export const increaseStock = async (productId, amount) => {
  const { data } = await api.post(API_ROUTES.admin.inventoryIncrease(productId), { amount });
  return unwrapApiResponse(data);
};

export const decreaseStock = async (productId, amount) => {
  const { data } = await api.post(API_ROUTES.admin.inventoryDecrease(productId), { amount });
  return unwrapApiResponse(data);
};

export const setStock = async (productId, quantity) => {
  const { data } = await api.put(API_ROUTES.admin.inventorySetStock(productId), { quantity });
  return unwrapApiResponse(data);
};

export const updateThreshold = async (productId, threshold) => {
  const { data } = await api.patch(API_ROUTES.admin.inventoryThreshold(productId), { threshold });
  return unwrapApiResponse(data);
};

export const fetchStockMovements = async (productId) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryMovements(productId));
  return unwrapApiResponse(data);
};
