import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const getMyProfile = async () => {
  const { data } = await api.get(API_ROUTES.customer.profile);
  return data;
};

export const updateMyProfile = async (profileData) => {
  const { data } = await api.put(API_ROUTES.customer.profile, profileData);
  return data;
};
