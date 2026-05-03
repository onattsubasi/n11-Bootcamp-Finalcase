import { useQuery } from '@tanstack/react-query';
import { fetchMe } from '../api/authApi';
import { useStore } from '../../../store';

export const useMe = () => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ['auth', 'me'],
    queryFn: fetchMe,
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
