import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const listAddresses = async () => {
  const { data } = await api.get(API_ROUTES.customer.addresses);
  return unwrapApiResponse(data);
};

export const getAddress = async (addressId) => {
  const { data } = await api.get(API_ROUTES.customer.addressById(addressId));
  return unwrapApiResponse(data);
};

export const createAddress = async (addressData) => {
  const { data } = await api.post(API_ROUTES.customer.addresses, addressData);
  return unwrapApiResponse(data);
};

export const updateAddress = async ({ addressId, addressData }) => {
  const { data } = await api.put(API_ROUTES.customer.addressById(addressId), addressData);
  return unwrapApiResponse(data);
};

export const deleteAddress = async (addressId) => {
  const { data } = await api.delete(API_ROUTES.customer.addressById(addressId));
  return unwrapApiResponse(data);
};

export const setDefaultShipping = async (addressId) => {
  const { data } = await api.post(API_ROUTES.customer.defaultShipping(addressId));
  return unwrapApiResponse(data);
};

export const setDefaultBilling = async (addressId) => {
  const { data } = await api.post(API_ROUTES.customer.defaultBilling(addressId));
  return unwrapApiResponse(data);
};
