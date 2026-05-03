import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as reviewApi from '../api/reviewApi';
import toast from 'react-hot-toast';

export const useProductReviews = (productId, params = {}) => {
  return useQuery({
    queryKey: ['product-reviews', productId, params],
    queryFn: () => reviewApi.fetchProductReviews(productId, params),
    enabled: !!productId,
  });
};

export const useProductRatingSummary = (productId) => {
  return useQuery({
    queryKey: ['product-rating-summary', productId],
    queryFn: () => reviewApi.fetchProductRatingSummary(productId),
    enabled: !!productId,
  });
};

export const useCreateReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewApi.createReview,
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['product-reviews', variables.productId] });
      queryClient.invalidateQueries({ queryKey: ['product-rating-summary', variables.productId] });
      toast.success('Review submitted successfully');
    },
    onError: (err) => toast.error(err.message || 'Failed to submit review'),
  });
};

export const useMyReviews = (params = {}) => {
  return useQuery({
    queryKey: ['my-reviews', params],
    queryFn: () => reviewApi.fetchMyReviews(params),
  });
};
