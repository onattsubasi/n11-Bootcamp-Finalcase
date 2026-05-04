import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Activity,
  ArrowLeft,
  Calendar,
  Clock,
  CreditCard,
  MapPin,
  Package,
  ShieldCheck,
  Truck,
  XCircle,
} from 'lucide-react';
import { useOrder, useCancelOrder } from '@/features/orders/hooks/useOrders';
import { Spinner } from '@/components/ui/Spinner';
import { OrderStatusBadge } from '@/components/ui/OrderStatusBadge';
import { StatusTimeline } from '@/components/ui/StatusTimeline';
import { formatCurrency, formatDate, formatDateTime } from '@/lib/utils/format';
import { Button } from '@/components/ui/Button';
import { errorMessage } from '@/api/problem';
import toast from 'react-hot-toast';

const OrderDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { data: order, isPending, isError } = useOrder(id || '');
  const { mutate: cancelOrder, isPending: isCancelling } = useCancelOrder();

  if (isPending) return (
    <div className="flex flex-col min-h-[60vh] items-center justify-center gap-4">
      <Spinner size="lg" />
      <span className="text-[10px] font-black uppercase tracking-[0.4em] text-muted-foreground animate-pulse">Loading order intelligence...</span>
    </div>
  );

  if (isError) return (
    <div className="max-w-xl mx-auto py-24 text-center px-6 bg-card border border-destructive/20 rounded-[3rem] shadow-xl mt-12">
      <XCircle className="w-16 h-16 text-destructive mx-auto mb-6 opacity-40" />
      <h2 className="text-2xl font-black text-foreground uppercase tracking-tight">Order Insight Unavailable</h2>
      <p className="text-muted-foreground font-medium mt-2 mb-8">We could not retrieve details for this transaction. Please verify the order ID.</p>
      <Button onClick={() => navigate('/orders')} variant="primary" className="rounded-2xl px-10 h-14 font-black uppercase tracking-widest text-xs">View All Orders</Button>
    </div>
  );

  if (!order) return null;

  const items = order.items || [];
  const statusHistory = (order as any).statusHistory || [];
  const rawMoney = (order as any).money || {};
  const money = {
    subtotalAmount: rawMoney.subtotalAmount ?? (order as any).subtotalAmount ?? 0,
    taxAmount: rawMoney.taxAmount ?? (order as any).taxAmount ?? 0,
    discountAmount: rawMoney.discountAmount ?? rawMoney.promotionDiscountAmount ?? (order as any).promotionDiscountAmount ?? 0,
  };
  const formatTRY = (amount?: number) => formatCurrency(amount ?? 0, order.currency || 'TRY');
  const canCancel = ['PENDING', 'PENDING_PAYMENT', 'PAID', 'PREPARING'].includes(order.status);

  const handleCancel = () => {
    if (globalThis.confirm('Request order cancellation?')) {
      cancelOrder(id || '', {
        onSuccess: () => toast.success('Cancellation request processed'),
        onError: (err: any) => toast.error(errorMessage(err) || 'Failed to cancel order')
      });
    }
  };

  return (
    <div className="mx-auto max-w-7xl space-y-12 py-16 px-4">
      {/* Header Section */}
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-8 bg-gray-950 text-white p-10 rounded-[3rem] shadow-2xl relative overflow-hidden border border-white/5">
        <div className="absolute top-0 right-0 w-80 h-80 bg-primary/10 rounded-full blur-[100px] -mr-40 -mt-40 pointer-events-none" />
        
        <div className="relative z-10 space-y-4">
          <button onClick={() => navigate('/orders')} className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-white/40 hover:text-primary transition-colors">
             <ArrowLeft size={14} /> Back to History
          </button>
          <div className="space-y-1">
             <p className="text-[10px] font-black uppercase tracking-[0.3em] text-primary">Transaction Protocol</p>
             <h1 className="text-5xl font-black tracking-tighter uppercase leading-none">Order #{order.orderNumber}</h1>
          </div>
          <div className="flex flex-wrap items-center gap-6 pt-2">
             <div className="flex items-center gap-2 text-white/60">
                <Calendar size={16} className="text-primary" />
                <span className="text-xs font-bold">{formatDate(order.createdAt)}</span>
             </div>
             <div className="h-4 w-px bg-white/10" />
             <OrderStatusBadge status={order.status} />
          </div>
        </div>

        <div className="relative z-10 flex gap-3">
          {canCancel && (
            <Button 
              variant="danger" 
              className="rounded-2xl h-14 px-8 font-black uppercase tracking-widest text-[10px] bg-red-500/10 text-red-500 border border-red-500/20 hover:bg-red-500 hover:text-white transition-all shadow-xl shadow-red-500/10"
              onClick={handleCancel}
              disabled={isCancelling}
            >
              {isCancelling ? 'Processing...' : 'Request Cancellation'}
            </Button>
          )}
          <Button variant="ghost" className="rounded-2xl h-14 px-8 font-black uppercase tracking-widest text-[10px] text-white/60 hover:text-white hover:bg-white/5">
             Download Invoice
          </Button>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
        <div className="lg:col-span-8 space-y-12">
          {/* Items Section */}
          <section className="bg-card rounded-[2.5rem] border border-border overflow-hidden shadow-2xl shadow-primary/5">
            <div className="bg-muted/30 px-10 py-6 border-b border-border flex items-center justify-between">
              <div className="flex items-center gap-3">
                 <Package className="text-primary" size={20} />
                 <h2 className="font-black text-foreground uppercase tracking-widest text-xs">Order Manifest</h2>
              </div>
              <span className="text-[10px] font-black text-muted-foreground uppercase tracking-widest">{items.length} Unique SKUs</span>
            </div>
            <div className="divide-y divide-border">
              {items.map((item: any) => (
                <div key={item.id} className="p-10 flex flex-col sm:flex-row items-center justify-between gap-8 group hover:bg-muted/10 transition-colors">
                  <div className="flex flex-col sm:flex-row items-center gap-8 text-center sm:text-left">
                    <div className="h-24 w-24 bg-muted rounded-[1.5rem] flex items-center justify-center p-3 border border-border group-hover:scale-105 transition-transform duration-500">
                       {item.imageUrl ? (
                         <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover rounded-lg" />
                       ) : (
                         <Package size={32} className="text-muted-foreground opacity-20" />
                       )}
                    </div>
                    <div className="space-y-1">
                      <span className="text-[9px] font-black text-primary uppercase tracking-[0.2em]">Verified SKU</span>
                      <p className="font-black text-xl text-foreground leading-tight">{item.productName}</p>
                      <div className="flex items-center justify-center sm:justify-start gap-4 pt-1">
                         <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                           {formatTRY(item.unitPrice)} <span className="text-muted-foreground/40 font-medium lowercase">/ unit</span>
                         </span>
                         <span className="h-1 w-1 bg-border rounded-full" />
                         <span className="text-[10px] font-black text-foreground uppercase tracking-widest">Qty: {item.quantity}</span>
                      </div>
                    </div>
                  </div>
                  <div className="text-center sm:text-right min-w-[140px]">
                    <p className="text-2xl font-black text-emerald-600 tabular-nums">
                      {formatTRY(item.lineTotal)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Status Timeline Section */}
          <section className="bg-card rounded-[2.5rem] border border-border p-10 shadow-2xl shadow-primary/5">
            <div className="flex items-center gap-3 mb-10">
               <Activity className="text-primary" size={20} />
               <h2 className="font-black text-foreground uppercase tracking-widest text-xs">Logistics Protocol History</h2>
            </div>
            <div className="pl-4">
               <StatusTimeline history={statusHistory} />
            </div>
          </section>
        </div>

        <div className="lg:col-span-4 space-y-8">
          {/* Financial Summary */}
          <section className="bg-gray-950 text-white rounded-[3rem] p-10 shadow-2xl relative overflow-hidden border border-white/5">
            <div className="absolute top-0 right-0 w-64 h-64 bg-primary/10 rounded-full blur-[80px] -mr-32 -mt-32 pointer-events-none" />
            
            <h2 className="text-xl font-black uppercase tracking-tighter text-white/90 mb-8 flex items-center gap-3">
               <ShieldCheck className="text-primary" size={20} /> Financial Review
            </h2>
            
            <div className="space-y-5 text-xs font-bold">
              <div className="flex justify-between text-white/40 uppercase tracking-widest">
                <span>Value Manifest</span>
                <span className="text-white/80 tabular-nums">{formatTRY(money.subtotalAmount)}</span>
              </div>
              <div className="flex justify-between text-white/40 uppercase tracking-widest">
                <span>Logistics Fee</span>
                <span className="text-emerald-400 font-black">Free</span>
              </div>
              <div className="flex justify-between text-white/40 uppercase tracking-widest">
                <span>Fiscal Tax (EST)</span>
                <span className="text-white/80 tabular-nums">{formatTRY(money.taxAmount)}</span>
              </div>
              {money.discountAmount > 0 ? (
                <div className="flex justify-between text-emerald-400 uppercase tracking-widest pt-2 border-t border-white/5">
                  <span>Privilege Rebate</span>
                  <span className="font-black tabular-nums">-{formatTRY(money.discountAmount)}</span>
                </div>
              ) : null}
              
              <div className="pt-6 mt-4 border-t border-white/10">
                 <div className="flex justify-between items-end">
                    <div className="space-y-1">
                       <p className="text-[9px] font-black uppercase tracking-[0.4em] text-white/20">Final Amount</p>
                       <p className="text-4xl font-black text-emerald-400 tracking-tighter tabular-nums leading-none">
                          {formatTRY(order.grandTotalAmount ?? 0)}
                       </p>
                    </div>
                 </div>
              </div>
            </div>
          </section>

          {/* Shipping Info */}
          <section className="bg-card rounded-[2.5rem] border border-border p-8 shadow-xl shadow-primary/5">
            <div className="flex items-center gap-3 mb-6">
               <MapPin className="text-primary" size={18} />
               <h2 className="font-black text-foreground uppercase tracking-widest text-[10px]">Logistics Target</h2>
            </div>
            {(order as any).shippingAddress ? (
              <div className="space-y-4">
                <div className="space-y-1">
                  <p className="text-xs font-black uppercase tracking-widest text-foreground">{(order as any).shippingAddress.title}</p>
                  <p className="text-sm font-medium text-muted-foreground leading-relaxed">
                    {(order as any).shippingAddress.addressLine1}<br />
                    {(order as any).shippingAddress.city}, {(order as any).shippingAddress.zipCode}<br />
                    {(order as any).shippingAddress.country}
                  </p>
                </div>
                
                {(order as any).shipmentSummary ? (
                  <div className="pt-6 border-t border-border space-y-3">
                    <div className="flex items-center gap-2">
                       <Truck className="text-primary" size={14} />
                       <p className="text-[10px] font-black text-foreground uppercase tracking-widest">Live Tracking</p>
                    </div>
                    <div className="bg-muted/50 p-4 rounded-2xl border border-border/50">
                       <p className="text-xs font-mono font-bold text-foreground">{(order as any).shipmentSummary.trackingNumber || 'Awaiting Sync...'}</p>
                       {(order as any).shipmentSummary.shippedAt && (
                         <p className="text-[10px] font-medium text-muted-foreground mt-2 italic">Dispatched: {formatDateTime((order as any).shipmentSummary.shippedAt)}</p>
                       )}
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-3 text-muted-foreground/60 p-4 bg-muted/20 rounded-2xl border border-dashed border-border mt-4">
                     <Clock size={16} />
                     <span className="text-[10px] font-black uppercase tracking-widest">Awaiting Fulfillment</span>
                  </div>
                )}
              </div>
            ) : null}
          </section>

          {/* Payment Section */}
          <section className="bg-card rounded-[2.5rem] border border-border p-8 shadow-xl shadow-primary/5">
            <div className="flex items-center gap-3 mb-6">
               <CreditCard className="text-primary" size={18} />
               <h2 className="font-black text-foreground uppercase tracking-widest text-[10px]">Payment Protocol</h2>
            </div>
            {(order as any).paymentSummary ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-widest">Interface</span>
                    <p className="text-xs font-black text-foreground uppercase">{(order as any).paymentSummary.methodType}</p>
                  </div>
                  <div className="space-y-1">
                    <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-widest">Security Gateway</span>
                    <p className="text-xs font-black text-foreground uppercase">{(order as any).paymentSummary.provider}</p>
                  </div>
                </div>
                <div className="pt-4 border-t border-border">
                  <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-widest">Encrypted Auth Token</span>
                  <p className="text-[10px] font-mono font-bold text-foreground truncate mt-1">{(order as any).paymentSummary.transactionId}</p>
                </div>
              </div>
            ) : (
              <div className="flex items-center gap-3 text-muted-foreground/60 p-4 bg-muted/20 rounded-2xl border border-dashed border-border">
                 <ShieldCheck size={16} />
                 <span className="text-[10px] font-black uppercase tracking-widest">Verification Pending</span>
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;
