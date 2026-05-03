import { useMutation, useQuery } from '@tanstack/react-query';
import { 
  fetchCheckoutQuote, 
  submitCheckout, 
  fetchCheckoutSession, 
  fetchMyCheckoutSessions 
} from '../api/checkoutApi';

/**
 * Hook for fetching a checkout quote.
 */
export const useCheckoutQuote = () => {
  return useMutation({
    mutationFn: fetchCheckoutQuote,
  });
};

/**
 * Hook for submitting a checkout.
 * Handles redirect logic if redirectUrl is present.
 */
export const useSubmitCheckout = () => {
  return useMutation({
    mutationFn: submitCheckout,
    onSuccess: (data) => {
      if (data.redirectUrl) {
        window.location.href = data.redirectUrl;
      }
    },
  });
};

/**
 * Hook for fetching a specific checkout session.
 */
export const useCheckoutSession = (id) => {
  return useQuery({
    queryKey: ['checkouts', id],
    queryFn: () => fetchCheckoutSession(id),
    enabled: !!id,
  });
};

/**
 * Hook for fetching my checkout sessions.
 */
export const useMyCheckoutSessions = (params = {}) => {
  return useQuery({
    queryKey: ['checkouts', 'me', params],
    queryFn: () => fetchMyCheckoutSessions(params),
  });
};
