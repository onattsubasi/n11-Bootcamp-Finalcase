import { useQuery } from '@tanstack/react-query';
import { fetchProduct } from '../api/catalogApi';
import { RawProduct } from '@/types/product';

export const useProduct = (id: string) => {
  return useQuery<RawProduct>({
    queryKey: ['product', id],
    queryFn: () => fetchProduct(id),
    enabled: !!id,
  });
};
