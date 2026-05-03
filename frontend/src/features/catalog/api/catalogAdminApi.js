import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

// Brands
export const fetchAdminBrands = async () => {
  const { data } = await api.get(API_ROUTES.admin.brands);
  return data;
};

export const createBrand = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.brands, payload);
  return data;
};

export const updateBrand = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.brandById(id), payload);
  return data;
};

export const deleteBrand = async (id) => {
  const { data } = await api.delete(API_ROUTES.admin.brandById(id));
  return data;
};

export const activateBrand = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.brandActivate(id));
  return data;
};

export const deactivateBrand = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.brandDeactivate(id));
  return data;
};

// Categories
export const fetchAdminCategories = async () => {
  const { data } = await api.get(API_ROUTES.admin.categories);
  return data;
};

export const createCategory = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.categories, payload);
  return data;
};

export const updateCategory = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.categoryById(id), payload);
  return data;
};

export const deleteCategory = async (id) => {
  const { data } = await api.delete(API_ROUTES.admin.categoryById(id));
  return data;
};

export const activateCategory = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.categoryActivate(id));
  return data;
};

export const deactivateCategory = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.deactivateCategory(id));
  return data;
};
