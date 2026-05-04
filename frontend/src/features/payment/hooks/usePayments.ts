import { useQuery } from '@tanstack/react-query';
import { fetchPayments, fetchPaymentById } from '../api/paymentApi';

/**
 * Hook for fetching customer payments.
 */
export const usePayments = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['payments', page, size],
    queryFn: () => fetchPayments(page, size),
  });
};

/**
 * Hook for fetching a specific payment.
 */
export const usePayment = (id) => {
  return useQuery({
    queryKey: ['payments', id],
    queryFn: () => fetchPaymentById(id),
    enabled: !!id,
  });
};
