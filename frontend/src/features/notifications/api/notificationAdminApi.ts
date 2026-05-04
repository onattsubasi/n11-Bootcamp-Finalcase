import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchAdminNotifications = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.notifications, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const createDirectNotification = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.notificationDirect, payload);
  return unwrapApiResponse(data);
};

export const retryDelivery = async (deliveryId) => {
  const { data } = await api.post(API_ROUTES.admin.notificationDeliveryRetry(deliveryId));
  return unwrapApiResponse(data);
};

export const upsertTemplate = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.notificationTemplates, payload);
  return unwrapApiResponse(data);
};

export const activateTemplate = async (id) => {
  const { data } = await api.patch(API_ROUTES.admin.notificationTemplateActivate(id));
  return unwrapApiResponse(data);
};

export const deactivateTemplate = async (id) => {
  const { data } = await api.patch(API_ROUTES.admin.notificationTemplateDeactivate(id));
  return unwrapApiResponse(data);
};
