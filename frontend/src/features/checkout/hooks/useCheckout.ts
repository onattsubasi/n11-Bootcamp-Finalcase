import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api';
import { API_ROUTES } from '@/api/routes';

export interface CheckoutQuoteParams {
  shippingAddressId: string;
  billingAddressId: string;
  couponCode?: string;
}

export interface CheckoutSubmitParams extends CheckoutQuoteParams {
  paymentMethod: {
    provider: string;
    methodType: string;
    paymentToken?: string | null;
    useThreeDSecure?: boolean;
  };
}

export const useCheckoutQuoteQuery = (params: CheckoutQuoteParams) => {
  return useQuery({
    queryKey: ['checkout-quote', params],
    queryFn: async () => {
      if (!params.shippingAddressId || !params.billingAddressId) return null;
      const { data } = await api.post(API_ROUTES.customer.checkoutQuote, params);
      return data?.data || data;
    },
    enabled: !!params.shippingAddressId && !!params.billingAddressId,
  });
};

export const useSubmitCheckout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (params: CheckoutSubmitParams) => {
      const { data } = await api.post(API_ROUTES.customer.checkoutSubmit, params);
      return data?.data || data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['basket'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
