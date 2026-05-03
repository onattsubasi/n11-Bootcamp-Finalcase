import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useOrders } from '../features/orders/hooks/useOrders';
import { OrderStatusBadge } from '../components/ui/OrderStatusBadge';
import { formatCurrency, formatDate } from '../lib/utils/format';

const Orders = () => {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError } = useOrders(page, 10);

  if (isLoading) return <div className="text-center p-10">Loading orders...</div>;
  if (isError) return <div className="text-center p-10 text-red-500">Failed to load orders.</div>;

  const orders = data?.content || [];
  const totalPages = data?.totalPages || 1;

  return (
    <div className="max-w-4xl mx-auto bg-white p-6 rounded shadow-md">
      <h2 className="text-2xl font-bold mb-6">My Orders</h2>
      
      {orders.length > 0 ? (
        <div className="flex flex-col gap-4">
          {orders.map((order) => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className="rounded border border-gray-200 bg-gray-50 p-4 transition-colors hover:border-blue-200 hover:bg-white"
            >
              <div className="flex items-center justify-between gap-4">
                <div>
                  <div className="font-bold text-lg text-gray-900">Order #{order.orderNumber}</div>
                  <div className="text-sm text-gray-500">Date: {formatDate(order.createdAt)}</div>
                  <div className="mt-2">
                    <OrderStatusBadge status={order.status} />
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xl font-bold text-gray-900">{formatCurrency(order.grandTotalAmount, order.currency)}</div>
                  <div className="mt-1 text-xs text-gray-500">View details</div>
                </div>
              </div>
            </Link>
          ))}

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-6">
              {[...Array(totalPages)].map((_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`px-3 py-1 rounded ${page === i ? 'bg-blue-600 text-white' : 'bg-gray-100 hover:bg-gray-200'}`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="text-center py-10 text-gray-500">No orders found.</div>
      )}
    </div>
  );
};

export default Orders;
