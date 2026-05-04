import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchNotifications = async (page = 0, size = 20, unreadOnly = false) => {
  const { data } = await api.get(API_ROUTES.customer.notifications, {
    params: { page, size, unreadOnly, unread: unreadOnly },
  });
  return unwrapApiResponse(data);
};

export const fetchUnreadCount = async () => {
  const { data } = await api.get(API_ROUTES.customer.notificationUnreadCount);
  return unwrapApiResponse(data);
};

export const markAsRead = async (notificationId: string) => {
  const { data } = await api.patch(API_ROUTES.customer.notificationRead(notificationId));
  return unwrapApiResponse(data);
};

export const fetchNotificationById = async (id: string) => {
  const { data } = await api.get(API_ROUTES.customer.notificationById(id));
  return unwrapApiResponse(data);
};

export const markAllRead = async () => {
  const { data } = await api.patch(API_ROUTES.customer.notificationReadAll);
  return unwrapApiResponse(data);
};
