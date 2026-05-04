import { useMutation } from '@tanstack/react-query';
import { logoutUser } from '../api/authApi';
import { queryClient } from '../../../lib/queryClient';
import { useStore } from '@/store';

export const useLogout = () => {
  const clearAuth = useStore((state) => state.clearAuth);
  const isAuthenticated = useStore((state) => state.isAuthenticated);

  return useMutation({
    mutationFn: async () => {
      if (isAuthenticated) {
        await logoutUser();
      }
    },
    onSettled: () => {
      clearAuth();
      queryClient.clear();
    },
  });
};
