import { cn } from '../../lib/utils/cn';

const STATUS_COLORS = {
  PENDING: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  AUTHORIZED: 'bg-blue-50 text-blue-700 border-blue-200',
  CAPTURED: 'bg-green-50 text-green-700 border-green-200',
  VOIDED: 'bg-gray-100 text-gray-800 border-gray-200',
  REFUNDED: 'bg-purple-50 text-purple-700 border-purple-200',
  PARTIALLY_REFUNDED: 'bg-purple-50 text-purple-700 border-purple-200',
  FAILED: 'bg-red-50 text-red-700 border-red-200',
  CANCELLED: 'bg-gray-50 text-gray-600 border-gray-200',
};

export const PaymentStatusBadge = ({ status }) => {
  const colorClass = STATUS_COLORS[status] || 'bg-gray-100 text-gray-800';

  return (
    <span className={cn(
      'px-2.5 py-0.5 rounded-full text-xs font-medium border',
      colorClass
    )}>
      {status?.replace(/_/g, ' ')}
    </span>
  );
};
