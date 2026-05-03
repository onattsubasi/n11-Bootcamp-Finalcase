import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchBasket, addToBasket, removeBasketItem } from '../api/basketApi';

export const useBasketQuery = (enabled = true) => {
  return useQuery({
    queryKey: ['basket'],
    queryFn: fetchBasket,
    enabled,
  });
};

export const useAddToCart = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: addToBasket,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};

export const useRemoveFromBasket = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: removeBasketItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
    },
  });
};
