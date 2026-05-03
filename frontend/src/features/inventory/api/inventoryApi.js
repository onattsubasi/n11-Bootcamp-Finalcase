import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchInventoryItems = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryItems, { params });
  return data;
};

export const fetchInventoryByProductId = async (productId) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryByProductId(productId));
  return data;
};

export const increaseStock = async (productId, amount) => {
  const { data } = await api.post(API_ROUTES.admin.inventoryIncrease(productId), { amount });
  return data;
};

export const decreaseStock = async (productId, amount) => {
  const { data } = await api.post(API_ROUTES.admin.inventoryDecrease(productId), { amount });
  return data;
};

export const setStock = async (productId, quantity) => {
  const { data } = await api.post(API_ROUTES.admin.inventorySetStock(productId), { quantity });
  return data;
};

export const updateThreshold = async (productId, threshold) => {
  const { data } = await api.post(API_ROUTES.admin.inventoryThreshold(productId), { threshold });
  return data;
};

export const fetchStockMovements = async (productId) => {
  const { data } = await api.get(API_ROUTES.admin.inventoryMovements(productId));
  return data;
};
