import api from '@/api';
import { API_ROUTES } from '@/api/routes';
import { normalizeProduct, normalizeProductPage } from '../utils/productMapper';
import { RawProduct, Category } from '@/types/product';
import { PageResponse } from '@/types/api';
import { unwrapApiResponse } from '@/lib/utils/api';

export const fetchProducts = async (page = 0, size = 20): Promise<PageResponse<RawProduct>> => {
  const { data } = await api.get(API_ROUTES.products.search, { params: { page, size } });
  return normalizeProductPage(data);
};

export const fetchProduct = async (id: string): Promise<RawProduct> => {
  const { data } = await api.get(API_ROUTES.products.byId(id));
  return normalizeProduct(unwrapApiResponse(data));
};

export const fetchCategories = async (): Promise<Category[]> => {
  return [];
};
