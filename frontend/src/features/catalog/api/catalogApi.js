import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchProducts = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.products.search, { params: { page, size } });
  return data;
};

export const fetchProduct = async (id) => {
  const { data } = await api.get(API_ROUTES.products.byId(id));
  return data;
};
