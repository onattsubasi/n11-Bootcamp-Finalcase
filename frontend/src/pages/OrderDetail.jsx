import { Link, useParams } from 'react-router-dom';
import { useOrder, useCancelOrder } from '../features/orders/hooks/useOrders';
import { Spinner } from '../components/ui/Spinner';
import { OrderStatusBadge } from '../components/ui/OrderStatusBadge';
import { StatusTimeline } from '../components/ui/StatusTimeline';
import { formatCurrency, formatDate, formatDateTime } from '../lib/utils/format';
import { Button } from '../components/ui/Button';
import toast from 'react-hot-toast';

const OrderDetail = () => {
  const { id } = useParams();
  const { data: order, isPending, isError } = useOrder(id);
  const { mutate: cancelOrder, isPending: isCancelling } = useCancelOrder();

  if (isPending) return <div className="flex min-h-96 items-center justify-center"><Spinner /></div>;
  if (isError) return <div className="p-10 text-center text-red-500">Failed to load order details.</div>;
  if (!order) return <div className="p-10 text-center text-gray-500">Order not found.</div>;

  const items = order.items || [];
  const statusHistory = order.statusHistory || [];
  const money = order.money || {};
  const canCancel = ['PENDING', 'PAID'].includes(order.status);

  const handleCancel = () => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      cancelOrder(id, {
        onSuccess: () => toast.success('Order cancelled successfully'),
        onError: (err) => toast.error(err.message || 'Failed to cancel order')
      });
    }
  };

  return (
    <div className="mx-auto max-w-5xl space-y-8 p-6">
      <div className="flex flex-wrap items-center justify-between gap-4 bg-white p-6 rounded-xl shadow-sm">
        <div>
          <p className="text-sm text-gray-500">Order #{order.orderNumber}</p>
          <h1 className="text-3xl font-bold text-gray-900">{formatDate(order.createdAt)}</h1>
        </div>
        <div className="flex items-center gap-4">
          <OrderStatusBadge status={order.status} />
          {canCancel && (
            <Button 
              variant="danger" 
              size="sm" 
              onClick={handleCancel}
              disabled={isCancelling}
            >
              {isCancelling ? 'Cancelling...' : 'Cancel Order'}
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-8">
          {/* Items */}
          <section className="bg-white rounded-xl shadow-sm overflow-hidden">
            <div className="bg-gray-50 px-6 py-4 border-b">
              <h2 className="font-bold text-gray-900">Order Items</h2>
            </div>
            <div className="divide-y">
              {items.map((item) => (
                <div key={item.id} className="p-6 flex items-center justify-between">
                  <div className="flex gap-4">
                    <div className="h-16 w-16 bg-gray-100 rounded flex items-center justify-center text-gray-400 text-xs font-bold">IMAGE</div>
                    <div>
                      <p className="font-bold text-gray-900">{item.productName}</p>
                      <p className="text-sm text-gray-500">
                        {formatCurrency(item.unitPrice, order.currency)} x {item.quantity}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-gray-900">{formatCurrency(item.lineTotal, order.currency)}</p>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Status History */}
          <section className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="font-bold text-gray-900 mb-6">Status History</h2>
            <StatusTimeline history={statusHistory} />
          </section>
        </div>

        <div className="space-y-8">
          {/* Summary */}
          <section className="bg-white rounded-xl shadow-sm p-6 space-y-4">
            <h2 className="font-bold text-gray-900">Summary</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Subtotal</span>
                <span className="font-medium">{formatCurrency(money.subtotalAmount, order.currency)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Shipping</span>
                <span className="font-medium">{formatCurrency(money.shippingAmount, order.currency)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tax</span>
                <span className="font-medium">{formatCurrency(money.taxAmount, order.currency)}</span>
              </div>
              {money.discountAmount > 0 && (
                <div className="flex justify-between text-green-600 font-medium">
                  <span>Discount</span>
                  <span>-{formatCurrency(money.discountAmount, order.currency)}</span>
                </div>
              )}
              <div className="flex justify-between text-lg font-bold border-t pt-2 mt-2">
                <span>Total</span>
                <span>{formatCurrency(order.grandTotalAmount, order.currency)}</span>
              </div>
            </div>
          </section>

          {/* Shipping */}
          <section className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="font-bold text-gray-900 mb-4">Shipping Info</h2>
            {order.shippingAddress && (
              <div className="text-sm space-y-1">
                <p className="font-bold">{order.shippingAddress.title}</p>
                <p className="text-gray-600">{order.shippingAddress.addressLine1}</p>
                <p className="text-gray-600">{order.shippingAddress.city}, {order.shippingAddress.zipCode}</p>
              </div>
            )}
            {order.shipmentSummary && (
              <div className="mt-4 pt-4 border-t border-dashed">
                <p className="text-xs text-gray-500 font-bold uppercase tracking-wider">Tracking</p>
                <p className="text-sm font-medium mt-1">{order.shipmentSummary.trackingNumber || 'Awaiting tracking info'}</p>
                {order.shipmentSummary.shippedAt && (
                  <p className="text-xs text-gray-500 mt-1">Shipped: {formatDateTime(order.shipmentSummary.shippedAt)}</p>
                )}
              </div>
            )}
          </section>

          {/* Payment */}
          <section className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="font-bold text-gray-900 mb-4">Payment Summary</h2>
            {order.paymentSummary ? (
              <div className="text-sm space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-500">Method</span>
                  <span className="font-medium uppercase">{order.paymentSummary.methodType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Provider</span>
                  <span className="font-medium uppercase">{order.paymentSummary.provider}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Transaction ID</span>
                  <span className="font-medium truncate ml-2 max-w-[150px]">{order.paymentSummary.transactionId}</span>
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-500 italic">No payment info available</p>
            )}
          </section>
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;