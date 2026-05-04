import { cn } from '../../lib/utils/cn';

const STATUS_COLORS = {
  INITIAL: 'bg-gray-100 text-gray-800 border-gray-200',
  QUOTE_GENERATED: 'bg-blue-50 text-blue-700 border-blue-200',
  WAITING_PAYMENT: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  PAYMENT_PENDING: 'bg-orange-50 text-orange-700 border-orange-200',
  PAYMENT_RECEIVED: 'bg-green-50 text-green-700 border-green-200',
  PAYMENT_FAILED: 'bg-red-50 text-red-700 border-red-200',
  COMPLETED: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  CANCELLED: 'bg-gray-50 text-gray-600 border-gray-200',
  FAILED: 'bg-red-100 text-red-800 border-red-300',
};

export const CheckoutStatusBadge = ({ status }) => {
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
