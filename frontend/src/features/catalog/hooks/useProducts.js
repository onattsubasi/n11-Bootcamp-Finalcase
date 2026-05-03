import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../api/catalogApi';

export const useProducts = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['products', page, size],
    queryFn: () => fetchProducts(page, size),
  });
};