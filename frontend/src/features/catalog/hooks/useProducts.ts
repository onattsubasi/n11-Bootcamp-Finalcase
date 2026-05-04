import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../api/catalogApi';
import { PageResponse } from '@/types/api';
import { RawProduct } from '@/types/product';

export const useProducts = (page = 0, size = 20) => {
  return useQuery<PageResponse<RawProduct>>({
    queryKey: ['products', page, size],
    queryFn: () => fetchProducts(page, size),
  });
};
