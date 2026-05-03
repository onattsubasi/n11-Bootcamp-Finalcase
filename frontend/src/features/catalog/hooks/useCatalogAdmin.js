import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as catalogAdminApi from '../api/catalogAdminApi';
import toast from 'react-hot-toast';

export const useAdminBrands = () => {
  return useQuery({
    queryKey: ['admin-brands'],
    queryFn: catalogAdminApi.fetchAdminBrands,
  });
};

export const useCreateBrand = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: catalogAdminApi.createBrand,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-brands'] });
      toast.success('Brand created successfully');
    },
    onError: (err) => toast.error(err.message || 'Failed to create brand'),
  });
};

export const useUpdateBrand = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: catalogAdminApi.updateBrand,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-brands'] });
      toast.success('Brand updated successfully');
    },
    onError: (err) => toast.error(err.message || 'Failed to update brand'),
  });
};

export const useAdminCategories = () => {
  return useQuery({
    queryKey: ['admin-categories'],
    queryFn: catalogAdminApi.fetchAdminCategories,
  });
};

export const useCreateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: catalogAdminApi.createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] });
      toast.success('Category created successfully');
    },
    onError: (err) => toast.error(err.message || 'Failed to create category'),
  });
};

export const useUpdateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: catalogAdminApi.updateCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] });
      toast.success('Category updated successfully');
    },
    onError: (err) => toast.error(err.message || 'Failed to update category'),
  });
};
