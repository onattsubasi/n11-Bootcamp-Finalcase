import { useState } from 'react';
import { useAdminPayments, useCreateRefund, useCancelPayment } from '../features/admin/hooks/useAdminPayment';
import { PaymentStatusBadge } from '../components/ui/PaymentStatusBadge';
import { Spinner } from '../components/ui/Spinner';
import { formatCurrency, formatDateTime } from '../lib/utils/format';
import { Button } from '../components/ui/Button';
import toast from 'react-hot-toast';

const AdminPayments = () => {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useAdminPayments(page, 10);
  const { mutate: refund, isPending: isRefunding } = useCreateRefund();
  const { mutate: cancel, isPending: isCancelling } = useCancelPayment();

  const payments = data?.content || [];
  const totalPages = data?.totalPages || 1;

  const handleRefund = (id) => {
    const amountStr = window.prompt('Enter refund amount:');
    if (!amountStr) return;
    const amount = parseFloat(amountStr);
    if (isNaN(amount)) return toast.error('Invalid amount');

    refund({ id, payload: { amount, reason: 'Admin requested refund' } }, {
      onSuccess: () => toast.success('Refund processed'),
      onError: (err) => toast.error(err.message || 'Refund failed')
    });
  };

  const handleCancel = (id) => {
    if (window.confirm('Cancel this payment?')) {
      cancel({ id, payload: { reason: 'Admin requested cancellation' } }, {
        onSuccess: () => toast.success('Payment cancelled'),
        onError: (err) => toast.error(err.message || 'Cancellation failed')
      });
    }
  };

  return (
    <div className='p-6 space-y-6'>
      <h1 className='text-2xl font-bold'>Payment Management</h1>

      {isLoading ? (
        <div className='flex justify-center p-20'><Spinner /></div>
      ) : (
        <div className='bg-white shadow rounded-lg overflow-hidden'>
          <table className='min-w-full divide-y divide-gray-200'>
            <thead className='bg-gray-50'>
              <tr>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Payment ID / Transaction</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Amount</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Status</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Date</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Actions</th>
              </tr>
            </thead>
            <tbody className='bg-white divide-y divide-gray-200'>
              {payments.map((p) => (
                <tr key={p.id}>
                  <td className='px-6 py-4 whitespace-nowrap'>
                    <div className='text-sm font-medium text-gray-900 truncate max-w-xs'>{p.transactionId || p.id}</div>
                    <div className='text-xs text-gray-500'>{p.provider} ({p.methodType})</div>
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900'>
                    {formatCurrency(p.amount, p.currency)}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap'>
                    <PaymentStatusBadge status={p.status} />
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    {formatDateTime(p.createdAt)}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2'>
                    {p.status === 'CAPTURED' && (
                      <Button size='sm' variant='outline' onClick={() => handleRefund(p.id)} disabled={isRefunding}>
                        Refund
                      </Button>
                    )}
                    {['AUTHORIZED', 'PENDING'].includes(p.status) && (
                      <Button size='sm' variant='danger' onClick={() => handleCancel(p.id)} disabled={isCancelling}>
                        Cancel
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {totalPages > 1 && (
            <div className='p-4 bg-gray-50 border-t flex justify-center gap-2'>
              {[...Array(totalPages)].map((_, i) => (
                <button 
                  key={i} 
                  onClick={() => setPage(i)}
                  className={`px-3 py-1 rounded ${page === i ? 'bg-blue-600 text-white' : 'bg-white border'}`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminPayments;
