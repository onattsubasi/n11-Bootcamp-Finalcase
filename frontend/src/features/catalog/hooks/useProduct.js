import { useQuery } from '@tanstack/react-query';
import { fetchProduct } from '../api/catalogApi';

export const useProduct = (id) => {
  return useQuery({
    queryKey: ['product', id],
    queryFn: () => fetchProduct(id),
    enabled: !!id,
  });
};
