import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  ShoppingBag, 
  ChevronRight, 
  Calendar, 
  Package, 
  History,
} from 'lucide-react';
import { useOrders } from '../features/orders/hooks/useOrders';
import { OrderStatusBadge } from '../components/ui/OrderStatusBadge';
import { formatCurrency, formatDate } from '../lib/utils/format';
import { Spinner } from '../components/ui/Spinner';
import { Button } from '../components/ui/Button';

const Orders: React.FC = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const { data, isLoading, isError } = useOrders(page, 10);

  if (isLoading) return (
    <div className="flex min-h-[50vh] items-center justify-center">
      <Spinner size="lg" />
    </div>
  );

  if (isError) return (
    <div className="max-w-xl mx-auto my-12 p-10 bg-red-50 border-2 border-dashed border-red-200 rounded-[2rem] text-center">
      <div className="text-4xl mb-4">❌</div>
      <h3 className="text-xl font-black text-red-900 uppercase">History Unavailable</h3>
      <p className="text-red-600 font-medium mt-2">We encountered an issue retrieving your purchase history. Please try again later.</p>
    </div>
  );

  const orders = data?.content || [];
  const totalPages = data?.totalPages || 1;

  return (
    <div className="max-w-5xl mx-auto py-12 px-4 space-y-10">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 border-b border-gray-100 pb-10">
        <div className="space-y-2">
           <div className="inline-flex items-center gap-2 px-3 py-1 bg-gray-100 rounded-full text-[10px] font-black uppercase tracking-widest text-gray-500">
              <History size={12} />
              <span>Purchase History</span>
           </div>
           <h1 className="text-4xl font-black text-gray-900 tracking-tighter uppercase">My Orders</h1>
        </div>
        
        <div className="flex items-center gap-4">
           <div className="bg-gray-50 px-6 py-3 rounded-2xl border border-gray-100 flex items-center gap-4">
              <div className="flex flex-col">
                 <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Total Orders</span>
                 <span className="text-xl font-black text-gray-900">{data?.totalElements || 0}</span>
              </div>
              <div className="h-8 w-px bg-gray-200" />
              <Package className="text-blue-600" />
           </div>
        </div>
      </header>
      
      {orders.length > 0 ? (
        <div className="space-y-6">
          <div className="grid gap-4">
            {orders.map((order) => (
              <Link
                key={order.id}
                to={`/orders/${order.id}`}
                className="group relative bg-white border border-gray-100 rounded-[2rem] p-6 lg:p-8 transition-all duration-500 hover:shadow-2xl hover:shadow-blue-900/5 hover:border-blue-100 flex flex-col md:flex-row items-start md:items-center justify-between gap-6"
              >
                <div className="flex items-center gap-6">
                   <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-all duration-500">
                      <ShoppingBag size={28} />
                   </div>
                   <div className="space-y-1">
                      <div className="flex items-center gap-3">
                         <span className="text-lg font-black text-gray-900 uppercase tracking-tighter">Order #{order.orderNumber}</span>
                         <OrderStatusBadge status={order.status} />
                      </div>
                      <div className="flex items-center gap-4 text-xs font-bold text-gray-400 uppercase tracking-widest">
                         <div className="flex items-center gap-1.5">
                            <Calendar size={14} />
                            <span>{formatDate(order.createdAt)}</span>
                         </div>
                         <div className="h-3 w-px bg-gray-200" />
                         <span>{(order as any).items?.length || 0} Items</span>
                      </div>
                   </div>
                </div>

                <div className="flex items-center gap-8 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 pt-4 md:pt-0">
                   <div className="flex flex-col items-end">
                      <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Amount Paid</span>
                      <span className="text-2xl font-black text-gray-900 tracking-tighter">
                         {formatCurrency(order.grandTotalAmount, order.currency)}
                      </span>
                   </div>
                   <div className="w-12 h-12 rounded-full bg-gray-50 flex items-center justify-center text-gray-300 group-hover:bg-blue-50 group-hover:text-blue-600 transition-all duration-500">
                      <ChevronRight size={24} />
                   </div>
                </div>
              </Link>
            ))}
          </div>

          {totalPages > 1 ? (
            <nav className="flex justify-center gap-3 pt-10">
              {[...Array(totalPages)].map((_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`
                    w-12 h-12 rounded-2xl font-black text-sm transition-all
                    ${page === i 
                      ? 'bg-blue-600 text-white shadow-xl shadow-blue-600/20' 
                      : 'bg-white border-2 border-gray-100 text-gray-400 hover:border-blue-200 hover:text-blue-600'
                    }
                  `}
                >
                  {i + 1}
                </button>
              ))}
            </nav>
          ) : null}
        </div>
      ) : (
        <div className="py-24 text-center bg-gray-50 rounded-[3rem] border-2 border-dashed border-gray-100">
          <div className="text-6xl mb-6">🏜️</div>
          <h3 className="text-2xl font-black text-gray-900 uppercase tracking-tight">No Orders Yet</h3>
          <p className="text-gray-500 font-medium max-w-sm mx-auto mt-2 mb-8">
            Your journey hasn't started yet. Browse our premium collections and place your first order today.
          </p>
          <Button onClick={() => navigate('/')} variant="primary" className="rounded-2xl px-10 h-14 font-black uppercase tracking-widest">
            Explore Marketplace
          </Button>
        </div>
      )}
    </div>
  );
};

export default Orders;
