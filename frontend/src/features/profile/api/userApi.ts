import api from '@/api';
import { API_ROUTES } from '@/api/routes';
import { unwrapApiResponse, unwrapPage } from '@/lib/utils/api';
import { PageResponse } from '@/types/api';

export interface CustomerPreference {
  id: string;
  theme: string;
  language: string;
  notificationsEnabled: boolean;
}

export interface Favorite {
  id: string;
  productId: string;
  productName?: string;
  imageUrl?: string;
  createdAt: string;
}

export interface ProductListItem {
  id?: string;
  productId: string;
  name?: string;
  productName?: string;
  imageUrl?: string;
  price?: number;
  note?: string;
}

export interface ProductList {
  id: string;
  name: string;
  description?: string;
  itemCount: number;
  isPublic: boolean;
  items?: ProductListItem[];
}

export interface Coupon {
  id: string;
  code: string;
  description?: string;
  expiryDate?: string;
  expiresAt?: string;
  status?: string;
  minimumOrderAmount?: number;
}

export interface AddListItemPayload {
  listId: string;
  productId: string;
  note?: string;
}

const normalizeArray = <T>(payload: unknown): T[] => {
  const source = unwrapApiResponse<any>(payload);

  if (Array.isArray(source)) {
    return source;
  }

  if (Array.isArray(source?.items)) {
    return source.items;
  }

  if (Array.isArray(source?.content)) {
    return source.content;
  }

  return [];
};

/**
 * Fetch customer preferences.
 */
export const fetchPreferences = async (): Promise<CustomerPreference> => {
  const { data } = await api.get(API_ROUTES.customer.preferences);
  return unwrapApiResponse<CustomerPreference>(data);
};

/**
 * Update customer preferences.
 */
export const updatePreferences = async (payload: Partial<CustomerPreference>): Promise<CustomerPreference> => {
  const { data } = await api.put(API_ROUTES.customer.preferences, payload);
  return unwrapApiResponse<CustomerPreference>(data);
};

/**
 * Fetch customer favorites.
 */
export const fetchFavorites = async (page = 0, size = 20): Promise<PageResponse<Favorite>> => {
  const { data } = await api.get(API_ROUTES.customer.favorites, { params: { page, size } });
  return unwrapPage<Favorite>(data);
};

/**
 * Add a product to favorites.
 */
export const addFavorite = async (productId: string): Promise<Favorite> => {
  const { data } = await api.post(API_ROUTES.customer.favorites, { productId });
  return unwrapApiResponse<Favorite>(data);
};

/**
 * Remove a product from favorites.
 */
export const removeFavorite = async (productId: string): Promise<void> => {
  const { data } = await api.delete(API_ROUTES.customer.favoriteByProductId(productId));
  return unwrapApiResponse<void>(data);
};

/**
 * List customer product lists.
 */
export const fetchProductLists = async (): Promise<ProductList[]> => {
  const { data } = await api.get(API_ROUTES.customer.productLists);
  return normalizeArray<ProductList>(data);
};

/**
 * Create a new product list.
 */
export const createProductList = async (payload: Partial<ProductList> | string): Promise<ProductList> => {
  const requestBody = typeof payload === 'string' ? { name: payload } : payload;
  const { data } = await api.post(API_ROUTES.customer.productLists, requestBody);
  return unwrapApiResponse<ProductList>(data);
};

/**
 * Update or rename a product list.
 */
export const updateProductList = async (
  listId: string,
  payload: Partial<ProductList>
): Promise<ProductList> => {
  const { data } = await api.put(API_ROUTES.customer.productListById(listId), payload);
  return unwrapApiResponse<ProductList>(data);
};

/**
 * Delete a product list.
 */
export const deleteProductList = async (listId: string): Promise<void> => {
  const { data } = await api.delete(API_ROUTES.customer.productListById(listId));
  return unwrapApiResponse<void>(data);
};

/**
 * Add an item to a product list.
 */
export const addProductListItem = async ({ listId, productId, note }: AddListItemPayload): Promise<void> => {
  const { data } = await api.post(API_ROUTES.customer.productListItem(listId), { productId, note: note || '' });
  return unwrapApiResponse<void>(data);
};

/**
 * Remove an item from a product list.
 */
export const removeProductListItem = async ({ listId, productId }: { listId: string; productId: string }): Promise<void> => {
  const { data } = await api.delete(API_ROUTES.customer.productListItemById(listId, productId));
  return unwrapApiResponse<void>(data);
};

/**
 * List assigned customer coupons.
 */
export const fetchCustomerCoupons = async (): Promise<PageResponse<Coupon>> => {
  const { data } = await api.get(API_ROUTES.customer.coupons);
  return unwrapPage<Coupon>(data);
};
