import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';

export const fetchProductReviews = async (productId, params = {}) => {
  const { data } = await api.get(API_ROUTES.products.reviewsByProductId(productId), { params });
  return data;
};

export const fetchProductRatingSummary = async (productId) => {
  const { data } = await api.get(API_ROUTES.products.ratingSummaryByProductId(productId));
  return data;
};

export const createReview = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.reviews, payload);
  return data;
};

export const updateReview = async (reviewId, payload) => {
  const { data } = await api.put(API_ROUTES.customer.reviewById(reviewId), payload);
  return data;
};

export const deleteReview = async (reviewId) => {
  const { data } = await api.delete(API_ROUTES.customer.reviewById(reviewId));
  return data;
};

export const fetchMyReviews = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.customer.reviews, { params });
  return data;
};
