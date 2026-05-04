import { useQuery } from '@tanstack/react-query';
import { fetchCustomerCoupons } from '../api/promotionApi';

/**
 * Hook to fetch customer's assigned coupons.
 */
export const useCustomerCoupons = () => {
  return useQuery({
    queryKey: ['customer-coupons'],
    queryFn: fetchCustomerCoupons,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
