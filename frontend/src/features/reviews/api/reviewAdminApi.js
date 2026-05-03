import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchAdminReviews = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.reviews, { params: { page, size } });
  return data;
};

export const approveReview = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewApprove(id));
  return data;
};

export const rejectReview = async (id, reason) => {
  const { data } = await api.post(API_ROUTES.admin.reviewReject(id), { reason });
  return data;
};

export const fetchAdminReviewReports = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.reviewReports, { params: { page, size } });
  return data;
};
