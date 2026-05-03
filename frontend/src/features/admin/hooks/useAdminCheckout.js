import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { 
  fetchAdminCheckouts, 
  fetchAdminCheckout, 
  retryFinalization, 
  retryCompensation 
} from '../api/adminCheckoutApi';

export const useAdminCheckouts = (params = {}) => {
  return useQuery({
    queryKey: ['admin', 'checkouts', params],
    queryFn: () => fetchAdminCheckouts(params),
  });
};

export const useAdminCheckout = (id) => {
  return useQuery({
    queryKey: ['admin', 'checkouts', id],
    queryFn: () => fetchAdminCheckout(id),
    enabled: !!id,
  });
};

export const useRetryFinalization = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: retryFinalization,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'checkouts', id] });
    },
  });
};

export const useRetryCompensation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: retryCompensation,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'checkouts', id] });
    },
  });
};
