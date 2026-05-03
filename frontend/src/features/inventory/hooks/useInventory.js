import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as inventoryApi from '../api/inventoryApi';
import toast from 'react-hot-toast';

export const useInventoryItems = (params = {}) => {
  return useQuery({
    queryKey: ['admin-inventory-items', params],
    queryFn: () => inventoryApi.fetchInventoryItems(params),
  });
};

export const useInventoryByProductId = (productId) => {
  return useQuery({
    queryKey: ['admin-inventory', productId],
    queryFn: () => inventoryApi.fetchInventoryByProductId(productId),
    enabled: !!productId,
  });
};

export const useIncreaseStock = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, amount }) => inventoryApi.increaseStock(productId, amount),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['admin-inventory-items'] });
      queryClient.invalidateQueries({ queryKey: ['admin-inventory', variables.productId] });
      toast.success('Stock increased successfully');
    },
  });
};

export const useDecreaseStock = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, amount }) => inventoryApi.decreaseStock(productId, amount),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['admin-inventory-items'] });
      queryClient.invalidateQueries({ queryKey: ['admin-inventory', variables.productId] });
      toast.success('Stock decreased successfully');
    },
  });
};

export const useSetStock = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, quantity }) => inventoryApi.setStock(productId, quantity),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['admin-inventory-items'] });
      queryClient.invalidateQueries({ queryKey: ['admin-inventory', variables.productId] });
      toast.success('Stock set successfully');
    },
  });
};
