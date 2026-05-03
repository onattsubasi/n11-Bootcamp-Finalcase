import { useCustomerShipments } from '../features/shipment/hooks/useShipment';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';

const CustomerShipments = () => {
  const { data, isLoading } = useCustomerShipments();
  const shipments = data?.content || data || [];

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">My Shipments</h1>
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Tracking Number</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Order ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Carrier</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {shipments.map(s => (
              <tr key={s.id}>
                <td className="px-6 py-4 text-sm text-gray-900 font-mono">{s.trackingNumber || 'Pending'}</td>
                <td className="px-6 py-4 text-sm text-gray-500">{s.orderId}</td>
                <td className="px-6 py-4">
                  <Badge tone={s.status === 'DELIVERED' ? 'success' : s.status === 'SHIPPED' ? 'info' : 'warning'}>
                    {s.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{s.carrier || 'N/A'}</td>
              </tr>
            ))}
            {!shipments.length && (
              <tr>
                <td colSpan="4" className="px-6 py-8 text-center text-gray-400">No shipments found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default CustomerShipments;
