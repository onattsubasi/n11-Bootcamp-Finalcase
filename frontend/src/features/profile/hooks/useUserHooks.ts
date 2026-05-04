import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchPreferences,
  updatePreferences,
  fetchFavorites,
  addFavorite,
  removeFavorite,
  fetchProductLists,
  createProductList,
  updateProductList,
  deleteProductList,
  addProductListItem,
  removeProductListItem,
  fetchCustomerCoupons,
  CustomerPreference,
  Favorite,
  ProductList,
  AddListItemPayload,
  Coupon,
} from '../api/userApi';
import { PageResponse } from '@/types/api';

/**
 * Hook for customer preferences.
 */
export const usePreferences = () => {
  return useQuery<CustomerPreference>({
    queryKey: ['preferences'],
    queryFn: fetchPreferences,
  });
};

export const useUpdatePreferences = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: Partial<CustomerPreference>) => updatePreferences(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] });
    },
  });
};

/**
 * Hook for customer favorites.
 */
export const useFavorites = (page = 0, size = 20) => {
  return useQuery<PageResponse<Favorite>>({
    queryKey: ['favorites', page, size],
    queryFn: () => fetchFavorites(page, size),
  });
};

export const useAddFavorite = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (productId: string) => addFavorite(productId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });
};

export const useRemoveFavorite = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (productId: string) => removeFavorite(productId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });
};

/**
 * Hooks for product lists.
 */
export const useProductLists = () => {
  return useQuery<ProductList[]>({
    queryKey: ['product-lists'],
    queryFn: fetchProductLists,
  });
};

export const useCreateProductList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: Partial<ProductList> | string) => createProductList(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
    },
  });
};

export const useRenameProductList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ listId, name }: { listId: string; name: string }) =>
      updateProductList(listId, { name }),
    onSuccess: (_, { listId }) => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
      queryClient.invalidateQueries({ queryKey: ['product-lists', listId] });
    },
  });
};

export const useDeleteProductList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (listId: string) => deleteProductList(listId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
    },
  });
};

export const useAddProductListItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: AddListItemPayload) => addProductListItem(payload),
    onSuccess: (_, { listId }) => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
      queryClient.invalidateQueries({ queryKey: ['product-lists', listId] });
    },
  });
};

export const useRemoveProductListItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: { listId: string; productId: string }) => removeProductListItem(payload),
    onSuccess: (_, { listId }) => {
      queryClient.invalidateQueries({ queryKey: ['product-lists'] });
      queryClient.invalidateQueries({ queryKey: ['product-lists', listId] });
    },
  });
};

/**
 * Backward-compatible alias used by older ProductListsTab.tsx versions.
 */
export const useRemoveFromProductList = useRemoveProductListItem;

/**
 * Hook for assigned customer coupons.
 */
export const useCoupons = () => {
  return useQuery<PageResponse<Coupon>>({
    queryKey: ['customer-coupons'],
    queryFn: fetchCustomerCoupons,
    staleTime: 5 * 60 * 1000,
  });
};
