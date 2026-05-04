import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as reviewAdminApi from '../api/reviewAdminApi';
import toast from 'react-hot-toast';

export const useAdminReviews = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-reviews', page, size],
    queryFn: () => reviewAdminApi.fetchAdminReviews(page, size),
  });
};

export const useApproveReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewAdminApi.approveReview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-reviews'] });
      toast.success('Review approved');
    },
  });
};

export const useRejectReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }) => reviewAdminApi.rejectReview(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-reviews'] });
      toast.success('Review rejected');
    },
  });
};

export const useAdminReviewReports = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-review-reports', page, size],
    queryFn: () => reviewAdminApi.fetchAdminReviewReports(page, size),
  });
};

export const useHideReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewAdminApi.hideReview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-reviews'] });
      toast.success('Review hidden');
    },
  });
};

export const useRestoreReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewAdminApi.restoreReview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-reviews'] });
      toast.success('Review restored');
    },
  });
};

export const useResolveReviewReport = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewAdminApi.resolveReviewReport,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-review-reports'] });
      toast.success('Report resolved');
    },
  });
};

export const useDismissReviewReport = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reviewAdminApi.dismissReviewReport,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-review-reports'] });
      toast.success('Report dismissed');
    },
  });
};
