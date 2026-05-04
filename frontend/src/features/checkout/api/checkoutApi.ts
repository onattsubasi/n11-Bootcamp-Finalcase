import api from '../../../api';
import { API_ROUTES } from '../../../api/routes';
import { unwrapApiResponse } from '../../../lib/utils/api';

const normalizeQuote = (payload) => {
  const quote = unwrapApiResponse(payload);
  if (!quote) return quote;

  return {
    ...quote,
    money: quote.money ?? {
      subtotalAmount: quote.subtotalAmount ?? 0,
      discountAmount: quote.promotionDiscountAmount ?? quote.discountAmount ?? 0,
      shippingAmount: quote.shippingFee ?? quote.shippingAmount ?? 0,
      taxAmount: quote.taxAmount ?? 0,
      grandTotalAmount: quote.grandTotalAmount ?? 0,
      currency: quote.currency ?? 'TRY',
    },
  };
};

/**
 * Fetch a checkout quote (order summary before payment).
 */
export const fetchCheckoutQuote = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.checkoutQuote, payload);
  return normalizeQuote(data);
};

/**
 * Submit checkout for finalization.
 * Requires Idempotency-Key.
 */
export const submitCheckout = async (payload) => {
  const { data } = await api.post(API_ROUTES.customer.checkoutSubmit, payload, {
    headers: {
      'Idempotency-Key': crypto.randomUUID(),
    },
  });
  return unwrapApiResponse(data);
};

/**
 * Fetch a specific checkout session by ID.
 */
export const fetchCheckoutSession = async (id) => {
  const { data } = await api.get(API_ROUTES.customer.checkoutById(id));
  return unwrapApiResponse(data);
};

/**
 * Fetch my recent checkout sessions.
 */
export const fetchMyCheckoutSessions = async (params = {}) => {
  const { data } = await api.get(API_ROUTES.customer.checkoutMe, { params });
  return unwrapApiResponse(data);
};
