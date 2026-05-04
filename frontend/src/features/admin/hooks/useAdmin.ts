import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  fetchAdminProducts,
  fetchAdminOrders,
  fetchAdminOrderById,
  fetchAdminOrderByNumber,
  cancelAdminOrder,
  markOrderPreparing,
  fetchAdminSearchDocuments,
  fetchAdminSearchDocument,
  fetchAdminUsers,
  fetchAdminUser,
  disableUser,
  activateUser,
  fetchAdminPromotions,
  fetchAdminPromotion,
  createPromotion,
  updatePromotion,
  activatePromotion,
  deletePromotion,
  fetchAdminCouponsByPromotion,
  createCoupon,
  createCouponBatch,
  deactivateCoupon,
  activateProduct,
  suspendProduct,
} from '../api/adminApi';

export const useAdminProducts = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-products', page, size],
    queryFn: () => fetchAdminProducts(page, size),
  });
};

export const useAdminOrders = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-orders', page, size],
    queryFn: () => fetchAdminOrders(page, size),
  });
};

export const useAdminOrder = (id) => {
  return useQuery({
    queryKey: ['admin-orders', id],
    queryFn: () => fetchAdminOrderById(id),
    enabled: !!id,
  });
};

export const useAdminOrderByNumber = (num) => {
  return useQuery({
    queryKey: ['admin-orders', 'number', num],
    queryFn: () => fetchAdminOrderByNumber(num),
    enabled: !!num,
  });
};

export const useCancelAdminOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }) => cancelAdminOrder(id, reason),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders', id] });
    },
  });
};

export const useMarkOrderPreparing = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: markOrderPreparing,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders', id] });
    },
  });
};

// Product Catalog Extensions
export const useActivateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: activateProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-products'] });
    },
  });
};

export const useSuspendProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: suspendProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-products'] });
    },
  });
};

export const useAdminSearchDocuments = (params = {}) => {
  return useQuery({
    queryKey: ['admin-search-documents', params],
    queryFn: () => fetchAdminSearchDocuments(params),
  });
};

export const useAdminSearchDocument = (productId) => {
  return useQuery({
    queryKey: ['admin-search-document', productId],
    queryFn: () => fetchAdminSearchDocument(productId),
    enabled: Boolean(productId),
  });
};

// User Admin Hooks
export const useAdminUsers = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-users', page, size],
    queryFn: () => fetchAdminUsers(page, size),
  });
};

export const useAdminUser = (id) => {
  return useQuery({
    queryKey: ['admin-users', id],
    queryFn: () => fetchAdminUser(id),
    enabled: !!id,
  });
};

export const useDisableUser = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: disableUser,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      queryClient.invalidateQueries({ queryKey: ['admin-users', id] });
    },
  });
};

export const useActivateUser = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: activateUser,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      queryClient.invalidateQueries({ queryKey: ['admin-users', id] });
    },
  });
};

// Promotion Admin Hooks
export const useAdminPromotions = (status) => {
  return useQuery({
    queryKey: ['admin-promotions', status],
    queryFn: () => fetchAdminPromotions(status),
  });
};

export const useAdminPromotion = (id) => {
  return useQuery({
    queryKey: ['admin-promotions', id],
    queryFn: () => fetchAdminPromotion(id),
    enabled: !!id,
  });
};

export const useCreatePromotion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createPromotion,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-promotions'] });
    },
  });
};

export const useUpdatePromotion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updatePromotion,
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['admin-promotions'] });
      queryClient.invalidateQueries({ queryKey: ['admin-promotions', id] });
    },
  });
};

export const useActivatePromotion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: activatePromotion,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-promotions'] });
      queryClient.invalidateQueries({ queryKey: ['admin-promotions', id] });
    },
  });
};

export const useDeletePromotion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deletePromotion,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-promotions'] });
    },
  });
};

// Coupon Admin Hooks
export const useAdminCouponsByPromotion = (promotionId) => {
  return useQuery({
    queryKey: ['admin-coupons', promotionId],
    queryFn: () => fetchAdminCouponsByPromotion(promotionId),
    enabled: !!promotionId,
  });
};

export const useCreateCoupon = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCoupon,
    onSuccess: (_, { promotionId }) => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons', promotionId] });
    },
  });
};

export const useCreateCouponBatch = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCouponBatch,
    onSuccess: (_, { promotionId }) => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons', promotionId] });
    },
  });
};

export const useDeactivateCoupon = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deactivateCoupon,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
    },
  });
};

