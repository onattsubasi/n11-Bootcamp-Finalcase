import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { cancelOrder, fetchOrders, requestReturn } from '../api/ordersApi';
import { fetchOrderById } from '../api/ordersApi';

export const useOrders = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['orders', page, size],
    queryFn: () => fetchOrders(page, size),
  });
};

export const useOrder = (orderId) => {
  return useQuery({
    queryKey: ['order', orderId],
    queryFn: () => fetchOrderById(orderId),
    enabled: Boolean(orderId),
  });
};

export const useCancelOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ orderId, reason }) => cancelOrder(orderId, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      if (variables?.orderId) {
        queryClient.invalidateQueries({ queryKey: ['order', variables.orderId] });
      }
    },
  });
};

export const useRequestReturn = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ orderId, reason }) => requestReturn(orderId, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      if (variables?.orderId) {
        queryClient.invalidateQueries({ queryKey: ['order', variables.orderId] });
      }
    },
  });
};
