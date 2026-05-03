import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchPreferences,
  updatePreferences,
  fetchFavorites,
  addFavorite,
  removeFavorite,
  fetchProductLists,
  createProductList,
  deleteProductList,
  addProductListItem,
  removeProductListItem,
} from '../api/userApi';

/**
 * Hook for customer preferences.
 */
export const usePreferences = () => {
  return useQuery({
    queryKey: ['preferences'],
    queryFn: fetchPreferences,
  });
};

export const useUpdatePreferences = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updatePreferences,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] });
    },
  });
};

/**
 * Hook for customer favorites.
 */
export const useFavorites = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['favorites', page, size],
    queryFn: () => fetchFavorites(page, size),
  });
};

export const useAddFavorite = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addFavorite,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });
};

export const useRemoveFavorite = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeFavorite,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });
};

/**
 * Hooks for product lists.
 */
export const useProductLists = () => {
  return useQuery({
    queryKey: ['product-lists'],
    queryFn: fetchProductLists,
  });
};

export const useCreateProductList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createProductList,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
    },
  });
};

export const useDeleteProductList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteProductList,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
    },
  });
};

export const useAddProductListItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addProductListItem,
    onSuccess: (_, { listId }) => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
      queryClient.invalidateQueries({ queryKey: ['product-lists', listId] });
    },
  });
};

export const useRemoveProductListItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeProductListItem,
    onSuccess: (_, { listId }) => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
      queryClient.invalidateQueries({ queryKey: ['product-lists', listId] });
    },
  });
};
