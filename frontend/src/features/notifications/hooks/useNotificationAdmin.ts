import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as notificationAdminApi from '../api/notificationAdminApi';
import toast from 'react-hot-toast';

export const useAdminNotifications = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-notifications', page, size],
    queryFn: () => notificationAdminApi.fetchAdminNotifications(page, size),
  });
};

export const useCreateDirectNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationAdminApi.createDirectNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-notifications'] });
      toast.success('Notification sent');
    },
  });
};

export const useRetryDelivery = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationAdminApi.retryDelivery,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-notifications'] });
      toast.success('Delivery retry initiated');
    },
  });
};
