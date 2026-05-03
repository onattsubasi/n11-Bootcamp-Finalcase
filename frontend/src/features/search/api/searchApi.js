import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

const buildSearchParams = (params = {}) => {
  const query = {};

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }

    query[key] = value;
  });

  return query;
};

export const searchProducts = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.products.search, { params: buildSearchParams(params) });
  return data;
};

export const fetchSearchFacets = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.products.searchFacets, { params: buildSearchParams(params) });
  return data;
};

export const fetchAutocomplete = async (query, limit = 10) => {
  const { data } = await api.get(API_ROUTES.products.autocomplete, { params: { query, limit } });
  return data;
};

