import { useQuery } from '@tanstack/react-query';
import { fetchCategories } from '../api/catalogApi';

export const useCategories = () => {
  return useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 1000 * 60 * 60, // 1 hour
  });
};
