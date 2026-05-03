import { useAdminOrders } from '../features/admin/hooks/useAdmin';

const AdminOrders = () => {
  const { data, isLoading, isError } = useAdminOrders(0, 20);

  if (isLoading) return <div className="p-4">Loading admin orders...</div>;
  if (isError) return <div className="p-4 text-red-500">Error loading orders.</div>;

  const orders = data?.content || data || [];

  return (
    <div className="bg-white p-4 rounded shadow">
      <h2 className="text-xl font-bold mb-4">Manage Orders</h2>
      {orders.length > 0 ? (
        <table className="w-full text-left">
          <thead>
            <tr className="border-b">
              <th className="py-2">Order ID</th>
              <th>Customer</th>
              <th>Total</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id || o.orderId} className="border-b">
                <td className="py-2">{o.id || o.orderId}</td>
                <td>{o.customerId}</td>
                <td>${o.totalPrice || o.totalAmount}</td>
                <td>{o.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <div className="text-gray-500">No orders.</div>
      )}
    </div>
  );
};

export default AdminOrders;
