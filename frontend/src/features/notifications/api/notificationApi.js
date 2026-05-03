import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchNotifications = async (page = 0, size = 20, unreadOnly = false) => {
  const { data } = await api.get(API_ROUTES.customer.notifications, { params: { page, size, unreadOnly } });
  return data;
};


export const fetchUnreadCount = async () => {
  const { data } = await api.get(API_ROUTES.customer.notificationUnreadCount);
  return data;
};

export const markAsRead = async (notificationId) => {
  const { data } = await api.patch(API_ROUTES.customer.notificationRead(notificationId));
  return data;
};

export const fetchNotificationById = async (id) => {
  const { data } = await api.get(API_ROUTES.customer.notificationById(id));
  return data;
};
