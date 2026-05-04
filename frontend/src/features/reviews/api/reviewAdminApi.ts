import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchAdminReviews = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.reviews, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const approveReview = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewApprove(id));
  return unwrapApiResponse(data);
};

export const rejectReview = async (id, reason) => {
  const { data } = await api.post(API_ROUTES.admin.reviewReject(id), { reason });
  return unwrapApiResponse(data);
};

export const fetchAdminReviewReports = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.reviewReports, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const hideReview = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewHide(id));
  return unwrapApiResponse(data);
};

export const restoreReview = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewRestore(id));
  return unwrapApiResponse(data);
};

export const resolveReviewReport = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewReportResolve(id));
  return unwrapApiResponse(data);
};

export const dismissReviewReport = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.reviewReportDismiss(id));
  return unwrapApiResponse(data);
};
