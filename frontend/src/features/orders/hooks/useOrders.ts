import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api';
import { API_ROUTES } from '@/api/routes';
import { PageResponse } from '@/types/api';

export interface Order {
  id: string;
  orderNumber: string;
  status: string;
  grandTotalAmount: number;
  currency: string;
  createdAt: string;
  items?: any[];
}

export const useOrders = (page = 0, size = 10) => {
  return useQuery<PageResponse<Order>>({
    queryKey: ['orders', page, size],
    queryFn: async () => {
      const { data } = await api.get(API_ROUTES.customer.orders, { params: { page, size } });
      return data?.data || data;
    },
  });
};

export const useOrder = (orderId: string) => {
  return useQuery<Order>({
    queryKey: ['order', orderId],
    queryFn: async () => {
      const { data } = await api.get(API_ROUTES.customer.orderById(orderId));
      return data?.data || data;
    },
    enabled: !!orderId,
  });
};

export const useCancelOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (orderId: string) => {
      const { data } = await api.post(API_ROUTES.customer.cancelOrder(orderId));
      return data?.data || data;
    },
    onSuccess: (_, orderId) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
    },
  });
};

export const useCheckout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await api.post(API_ROUTES.customer.checkoutSubmit, payload);
      return data?.data || data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
