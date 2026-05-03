import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as shipmentApi from '../api/shipmentApi';
import toast from 'react-hot-toast';

export const useCustomerShipments = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['shipments', page, size],
    queryFn: () => shipmentApi.fetchCustomerShipments(page, size),
  });
};

export const useAdminShipments = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ['admin-shipments', page, size],
    queryFn: () => shipmentApi.fetchAdminShipments(page, size),
  });
};

export const useChangeShipmentStatus = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }) => shipmentApi.changeShipmentStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-shipments'] });
      toast.success('Shipment status updated');
    },
  });
};
