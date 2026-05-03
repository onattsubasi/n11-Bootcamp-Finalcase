import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as notificationApi from '../api/notificationApi';

export const useNotifications = (page = 0, size = 20, unreadOnly = false) => {
  return useQuery({
    queryKey: ['notifications', page, size, unreadOnly],
    queryFn: () => notificationApi.fetchNotifications(page, size, unreadOnly),
  });
};

export const useUnreadCount = () => {
  return useQuery({
    queryKey: ['unread-notifications-count'],
    queryFn: notificationApi.fetchUnreadCount,
    refetchInterval: 1000 * 60, // Every minute
  });
};

export const useMarkNotificationRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationApi.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unread-notifications-count'] });
    },
  });
};

