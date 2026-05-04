import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from '../api/basketApi';

export const useBasketQuery = (enabled = true) => {
  return useQuery({
    queryKey: ['basket'],
    queryFn: api.fetchBasket,
    enabled,
  });
};

export const useAddToCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.addToBasket,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};

export const useUpdateBasketItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.updateBasketItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};

export const useRemoveFromBasket = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.removeBasketItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};

export const useClearBasket = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.clearBasket,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};
