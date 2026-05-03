import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchNotifications = async (page = 0, size = 10, unread = false) => {
  const response = await api.get(API_ROUTES.customer.notifications, {
    params: { page, size, unread },
  });
  return response.data.data;
};

export const markNotificationRead = async (notificationId) => {
  const response = await api.patch(API_ROUTES.customer.notificationRead(notificationId));
  return response.data.data;
};
