import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

// Product reviews (public, no auth required)
export const fetchProductReviews = async (productId, page = 0, size = 10) => {
  const response = await api.get(API_ROUTES.products.reviewsByProductId(productId), {
    params: { page, size },
  });
  return response.data.data;
};

export const fetchProductRatingSummary = async (productId) => {
  const response = await api.get(API_ROUTES.products.ratingSummaryByProductId(productId));
  return response.data.data;
};

// Customer reviews (auth required)
export const fetchCustomerReviews = async (page = 0, size = 10) => {
  const response = await api.get(API_ROUTES.customer.reviews, {
    params: { page, size },
  });
  return response.data.data;
};

export const createReview = async (reviewData) => {
  const response = await api.post(API_ROUTES.customer.reviews, reviewData);
  return response.data.data;
};

export const updateReview = async (reviewId, reviewData) => {
  const response = await api.put(API_ROUTES.customer.reviewById(reviewId), reviewData);
  return response.data.data;
};

export const deleteReview = async (reviewId) => {
  await api.delete(API_ROUTES.customer.reviewById(reviewId));
  return { success: true };
};
