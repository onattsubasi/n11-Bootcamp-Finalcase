import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchCustomerShipments = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.customer.shipments, { params: { page, size } });
  return data;
};

export const fetchCustomerShipment = async (id) => {
  const { data } = await api.get(API_ROUTES.customer.shipmentById(id));
  return data;
};

export const fetchAdminShipments = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.shipments, { params: { page, size } });
  return data;
};

export const fetchAdminShipment = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.shipmentById(id));
  return data;
};

export const cancelShipment = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.shipmentCancel(id));
  return data;
};

export const changeShipmentStatus = async (id, status) => {
  const { data } = await api.patch(API_ROUTES.admin.shipmentStatus(id), { status });
  return data;
};

export const updateShipmentTracking = async (id, payload) => {
  const { data } = await api.patch(API_ROUTES.admin.shipmentTracking(id), payload);
  return data;
};
