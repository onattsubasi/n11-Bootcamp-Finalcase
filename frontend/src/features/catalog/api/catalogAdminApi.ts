import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

// Brands
export const fetchAdminBrands = async () => {
  const { data } = await api.get(API_ROUTES.admin.brands);
  return unwrapApiResponse(data);
};

export const createBrand = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.brands, payload);
  return unwrapApiResponse(data);
};

export const updateBrand = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.brandById(id), payload);
  return unwrapApiResponse(data);
};

export const deleteBrand = async (id) => {
  const { data } = await api.delete(API_ROUTES.admin.brandById(id));
  return unwrapApiResponse(data);
};

export const activateBrand = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.brandActivate(id));
  return unwrapApiResponse(data);
};

export const deactivateBrand = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.brandDeactivate(id));
  return unwrapApiResponse(data);
};

export const suspendBrand = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.brandSuspend(id));
  return unwrapApiResponse(data);
};

// Categories
export const fetchAdminCategories = async () => {
  const { data } = await api.get(API_ROUTES.admin.categories);
  return unwrapApiResponse(data);
};

export const createCategory = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.categories, payload);
  return unwrapApiResponse(data);
};

export const updateCategory = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.categoryById(id), payload);
  return unwrapApiResponse(data);
};

export const deleteCategory = async (id) => {
  const { data } = await api.delete(API_ROUTES.admin.categoryById(id));
  return unwrapApiResponse(data);
};

export const activateCategory = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.categoryActivate(id));
  return unwrapApiResponse(data);
};

export const deactivateCategory = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.categoryDeactivate(id));
  return unwrapApiResponse(data);
};

export const suspendCategory = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.categorySuspend(id));
  return unwrapApiResponse(data);
};
