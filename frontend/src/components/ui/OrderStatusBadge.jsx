import { Badge } from './Badge';

const STATUS_TONES = {
  PENDING: 'warning',
  PAID: 'info',
  PROCESSING: 'info',
  SHIPPED: 'success',
  COMPLETED: 'success',
  DELIVERED: 'success',
  CANCELLED: 'danger',
  FAILED: 'danger',
  RETURN_REQUESTED: 'warning',
  RETURNED: 'neutral',
};

const STATUS_LABELS = {
  PENDING: 'Pending',
  PAID: 'Paid',
  PROCESSING: 'Processing',
  SHIPPED: 'Shipped',
  COMPLETED: 'Completed',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  FAILED: 'Failed',
  RETURN_REQUESTED: 'Return Requested',
  RETURNED: 'Returned',
};

export const OrderStatusBadge = ({ status = 'PENDING' }) => {
  const tone = STATUS_TONES[status] ?? 'neutral';
  const label = STATUS_LABELS[status] ?? status;

  return <Badge tone={tone}>{label}</Badge>;
};

export default OrderStatusBadge;