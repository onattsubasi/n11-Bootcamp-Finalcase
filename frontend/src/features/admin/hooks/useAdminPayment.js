import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { 
  fetchAdminPayments, 
  fetchAdminPayment, 
  fetchPaymentRefunds, 
  createRefund, 
  cancelPayment 
} from '../api/adminPaymentApi';

export const useAdminPayments = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['admin', 'payments', page, size],
    queryFn: () => fetchAdminPayments(page, size),
  });
};

export const useAdminPayment = (id) => {
  return useQuery({
    queryKey: ['admin', 'payments', id],
    queryFn: () => fetchAdminPayment(id),
    enabled: !!id,
  });
};

export const usePaymentRefunds = (id, page = 0, size = 10) => {
  return useQuery({
    queryKey: ['admin', 'payments', id, 'refunds', page, size],
    queryFn: () => fetchPaymentRefunds(id, page, size),
    enabled: !!id,
  });
};

export const useCreateRefund = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => createRefund(id, payload),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'payments', id] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'payments', id, 'refunds'] });
    },
  });
};

export const useCancelPayment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => cancelPayment(id, payload),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'payments', id] });
    },
  });
};
