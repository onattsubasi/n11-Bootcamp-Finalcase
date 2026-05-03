import { useAdminShipments, useChangeShipmentStatus } from '../features/shipment/hooks/useShipment';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';

const AdminShipments = () => {
  const { data, isLoading } = useAdminShipments();
  const updateStatusMutation = useChangeShipmentStatus();

  const shipments = data?.content || data || [];

  const handleUpdateStatus = (id, status) => {
    updateStatusMutation.mutate({ id, status });
  };

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Manage Shipments</h1>

      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Shipment ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Order ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Carrier</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {shipments.map(s => (
              <tr key={s.id}>
                <td className="px-6 py-4 text-sm text-gray-500 font-mono">{s.id.substring(0, 8)}...</td>
                <td className="px-6 py-4 text-sm text-gray-900">{s.orderId}</td>
                <td className="px-6 py-4">
                  <Badge tone={s.status === 'DELIVERED' ? 'success' : s.status === 'SHIPPED' ? 'info' : 'warning'}>
                    {s.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{s.carrier || 'N/A'}</td>
                <td className="px-6 py-4 text-right flex gap-1 justify-end">
                  <Button variant="outline" size="xs" onClick={() => handleUpdateStatus(s.id, 'SHIPPED')}>Mark Shipped</Button>
                  <Button variant="outline" size="xs" onClick={() => handleUpdateStatus(s.id, 'DELIVERED')}>Mark Delivered</Button>
                </td>
              </tr>
            ))}
            {!shipments.length && (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-gray-400">No shipments found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminShipments;
