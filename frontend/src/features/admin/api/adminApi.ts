import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

export const fetchAdminProducts = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.products, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const fetchAdminOrders = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.orders, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const fetchAdminOrderById = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.orderById(id));
  return unwrapApiResponse(data);
};

export const fetchAdminOrderByNumber = async (num) => {
  const { data } = await api.get(API_ROUTES.admin.orderByNumber(num));
  return unwrapApiResponse(data);
};

export const cancelAdminOrder = async (id, reason) => {
  const { data } = await api.post(API_ROUTES.admin.cancelOrder(id), { reason });
  return unwrapApiResponse(data);
};

export const markOrderPreparing = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.preparingOrder(id));
  return unwrapApiResponse(data);
};

export const fetchAdminSearchDocuments = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.admin.searchDocuments, { params });
  return unwrapApiResponse(data);
};

export const fetchAdminSearchDocument = async (productId) => {
  const { data } = await api.get(API_ROUTES.admin.searchDocumentByProductId(productId));
  return unwrapApiResponse(data);
};

export const reindexProductSearch = async (productId) => {
  const { data } = await api.post(API_ROUTES.admin.searchReindex(productId));
  return unwrapApiResponse(data);
};

export const rebuildSearchIndex = async () => {
  const { data } = await api.post(API_ROUTES.admin.searchRebuild);
  return unwrapApiResponse(data);
};

// User Management
export const fetchAdminUsers = async (page = 0, size = 20) => {
  const { data } = await api.get(API_ROUTES.admin.users, { params: { page, size } });
  return unwrapApiResponse(data);
};

export const fetchAdminUser = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.userById(id));
  return unwrapApiResponse(data);
};

export const disableUser = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.userDisable(id));
  return unwrapApiResponse(data);
};

export const activateUser = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.userActivate(id));
  return unwrapApiResponse(data);
};

// Product Management Extensions
export const activateProduct = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.productActivate(id));
  return unwrapApiResponse(data);
};

export const suspendProduct = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.productSuspend(id));
  return unwrapApiResponse(data);
};

// Promotion & Coupon Management
export const fetchAdminPromotions = async (status) => {
  const { data } = await api.get(API_ROUTES.admin.promotions, { params: { status } });
  return unwrapApiResponse(data);
};

export const fetchAdminPromotion = async (id) => {
  const { data } = await api.get(API_ROUTES.admin.promotionById(id));
  return unwrapApiResponse(data);
};

export const createPromotion = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.promotions, payload);
  return unwrapApiResponse(data);
};

export const updatePromotion = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.promotionById(id), payload);
  return unwrapApiResponse(data);
};

export const activatePromotion = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.promotionActivate(id));
  return unwrapApiResponse(data);
};

export const pausePromotion = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.promotionPause(id));
  return unwrapApiResponse(data);
};

export const expirePromotion = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.promotionExpire(id));
  return unwrapApiResponse(data);
};

export const deletePromotion = async (id) => {
  const { data } = await api.delete(API_ROUTES.admin.promotionById(id));
  return unwrapApiResponse(data);
};

export const fetchAdminCouponsByPromotion = async (promotionId) => {
  const { data } = await api.get(API_ROUTES.admin.couponsByPromotionId(promotionId));
  return unwrapApiResponse(data);
};

export const createCoupon = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.coupons, payload);
  return unwrapApiResponse(data);
};

export const updateCoupon = async ({ id, payload }) => {
  const { data } = await api.put(API_ROUTES.admin.couponById(id), payload);
  return unwrapApiResponse(data);
};

export const activateCoupon = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.couponActivate(id));
  return unwrapApiResponse(data);
};

export const pauseCoupon = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.couponPause(id));
  return unwrapApiResponse(data);
};

export const expireCoupon = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.couponExpire(id));
  return unwrapApiResponse(data);
};

export const deactivateCoupon = async (id) => {
  const { data } = await api.post(API_ROUTES.admin.couponDeactivate(id));
  return unwrapApiResponse(data);
};


export const createCouponBatch = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.couponBatch, payload);
  return unwrapApiResponse(data);
};

export const assignCoupon = async (payload) => {
  const { data } = await api.post(API_ROUTES.admin.couponAssignments, payload);
  return unwrapApiResponse(data);
};

// Review Moderation
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
