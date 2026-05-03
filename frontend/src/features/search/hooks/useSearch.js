import { useQuery } from '@tanstack/react-query';
import { fetchSearchFacets, searchProducts, fetchAutocomplete } from '../api/searchApi';

export const useSearch = (params) => {
  return useQuery({
    queryKey: ['search', params],
    queryFn: () => searchProducts(params),
    enabled: Boolean(params?.q || params?.categoryId || params?.brandIds || params?.minPrice || params?.maxPrice || params?.inStock || params?.hasPromotion || params?.sort),
  });
};

export const useSearchFacets = (params) => {
  return useQuery({
    queryKey: ['searchFacets', params],
    queryFn: () => fetchSearchFacets(params),
    enabled: Boolean(params?.q || params?.categoryId || params?.brandIds || params?.minPrice || params?.maxPrice || params?.inStock || params?.hasPromotion || params?.sort),
  });
};

export const useAutocomplete = (query, limit = 10) => {
  return useQuery({
    queryKey: ['autocomplete', query, limit],
    queryFn: () => fetchAutocomplete(query, limit),
    enabled: Boolean(query && query.length >= 2),
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
};

