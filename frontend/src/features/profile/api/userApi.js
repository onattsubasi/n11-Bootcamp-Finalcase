import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

/**
 * Fetch customer preferences.
 */
export const fetchPreferences = async () => {
  const { data } = await api.get(API_ROUTES.customer.preferences);
  return data;
};

/**
 * Update customer preferences.
 */
export const updatePreferences = async (payload) => {
  const { data } = await api.put(API_ROUTES.customer.preferences, payload);
  return data;
};

/**
 * Fetch customer favorites.
 */
export const fetchFavorites = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.customer.favorites, { params: { page, size } });
  return data;
};

/**
 * Add a product to favorites.
 */
export const addFavorite = async (productId) => {
  const { data } = await api.post(API_ROUTES.customer.favorites, { productId });
  return data;
};

/**
 * Remove a product from favorites.
 */
export const removeFavorite = async (productId) => {
  const { data } = await api.delete(API_ROUTES.customer.favoriteByProductId(productId));
  return data;
};

/**
 * List customer product lists.
 */
export const fetchProductLists = async () => {
  const { data } = await api.get(API_ROUTES.customer.productLists);
  return data;
};

/**
 * Create a new product list.
 */
export const createProductList = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.productLists, payload);
  return data;
};

/**
 * Delete a product list.
 */
export const deleteProductList = async (listId) => {
  const { data } = await api.delete(API_ROUTES.customer.productListById(listId));
  return data;
};

/**
 * Add an item to a product list.
 */
export const addProductListItem = async ({ listId, productId, note }) => {
  const { data } = await api.post(API_ROUTES.customer.productListItem(listId), { productId, note });
  return data;
};

/**
 * Remove an item from a product list.
 */
export const removeProductListItem = async ({ listId, productId }) => {
  const { data } = await api.delete(API_ROUTES.customer.productListItemById(listId, productId));
  return data;
};
