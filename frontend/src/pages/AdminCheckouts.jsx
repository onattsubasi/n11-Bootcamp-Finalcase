import { useState } from 'react';
import { useAdminCheckouts, useRetryFinalization, useRetryCompensation } from '../features/admin/hooks/useAdminCheckout';
import { CheckoutStatusBadge } from '../components/ui/CheckoutStatusBadge';
import { Spinner } from '../components/ui/Spinner';
import { formatDateTime } from '../lib/utils/format';
import { Button } from '../components/ui/Button';
import toast from 'react-hot-toast';

const AdminCheckouts = () => {
  const [status, setStatus] = useState('');
  const { data, isLoading } = useAdminCheckouts({ status: status || undefined });
  const { mutate: retryFinalize, isPending: isFinalizing } = useRetryFinalization();
  const { mutate: retryCompensate, isPending: isCompensating } = useRetryCompensation();

  const checkouts = data?.content || [];

  return (
    <div className='p-6 space-y-6'>
      <div className='flex justify-between items-center'>
        <h1 className='text-2xl font-bold'>Checkout Session Management</h1>
        <select 
          className='border rounded p-2' 
          value={status} 
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value=''>All Statuses</option>
          <option value='INITIAL'>Initial</option>
          <option value='QUOTE_GENERATED'>Quote Generated</option>
          <option value='WAITING_PAYMENT'>Waiting Payment</option>
          <option value='PAYMENT_PENDING'>Payment Pending</option>
          <option value='COMPLETED'>Completed</option>
          <option value='FAILED'>Failed</option>
          <option value='CANCELLED'>Cancelled</option>
        </select>
      </div>

      {isLoading ? (
        <div className='flex justify-center p-20'><Spinner /></div>
      ) : (
        <div className='bg-white shadow rounded-lg overflow-hidden'>
          <table className='min-w-full divide-y divide-gray-200'>
            <thead className='bg-gray-50'>
              <tr>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Customer</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Status</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Created At</th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Actions</th>
              </tr>
            </thead>
            <tbody className='bg-white divide-y divide-gray-200'>
              {checkouts.map((session) => (
                <tr key={session.id}>
                  <td className='px-6 py-4 whitespace-nowrap'>
                    <div className='text-sm font-medium text-gray-900'>{session.customerEmail || session.customerId}</div>
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap'>
                    <CheckoutStatusBadge status={session.status} />
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    {formatDateTime(session.createdAt)}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2'>
                    {session.status === 'FAILED' && (
                      <>
                        <Button 
                          size='sm' 
                          variant='outline' 
                          onClick={() => retryFinalize(session.id, { onSuccess: () => toast.success('Retrying finalization...') })}
                          disabled={isFinalizing}
                        >
                          Retry Finalize
                        </Button>
                        <Button 
                          size='sm' 
                          variant='outline'
                          onClick={() => retryCompensate(session.id, { onSuccess: () => toast.success('Retrying compensation...') })}
                          disabled={isCompensating}
                        >
                          Retry Compensate
                        </Button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!checkouts.length && <div className='p-10 text-center text-gray-500'>No checkout sessions found.</div>}
        </div>
      )}
    </div>
  );
};

export default AdminCheckouts;
