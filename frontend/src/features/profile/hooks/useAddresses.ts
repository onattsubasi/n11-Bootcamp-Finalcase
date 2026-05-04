import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  listAddresses,
  getAddress,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultShipping,
  setDefaultBilling,
} from '../api/addressesApi';

export const useAddresses = () => {
  return useQuery({
    queryKey: ['addresses'],
    queryFn: listAddresses,
  });
};

export const useAddress = (addressId) => {
  return useQuery({
    queryKey: ['addresses', addressId],
    queryFn: () => getAddress(addressId),
    enabled: !!addressId,
  });
};

export const useCreateAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });
};

export const useUpdateAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });
};

export const useDeleteAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });
};

export const useSetDefaultShipping = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setDefaultShipping,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });
};

export const useSetDefaultBilling = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setDefaultBilling,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });
};
