import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  CreditCard, 
  MapPin, 
  Tag, 
  ShieldCheck, 
  ChevronRight, 
  PackageCheck,
  CheckCircle2,
  AlertCircle,
  Clock,
  ArrowLeft
} from 'lucide-react';
import { useCheckoutQuoteQuery, useSubmitCheckout } from '@/features/checkout/hooks/useCheckout';
import { useAddresses } from '@/features/profile/hooks/useAddresses';
import { useBasketQuery } from '@/features/basket/hooks/useBasket';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { formatTRY } from '@/lib/utils/format';
import { Spinner } from '@/components/ui/Spinner';
import { errorMessage } from '@/api/problem';
import toast from 'react-hot-toast';

const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const { data: basket, isLoading: isBasketLoading } = useBasketQuery(true);
  const { data: addresses, isLoading: isAddressesLoading } = useAddresses();
  const { mutate: submitCheckout, isPending: isSubmitting } = useSubmitCheckout();
  
  const [userSelectedShippingId, setUserSelectedShippingId] = useState<string>('');
  const [userSelectedBillingId, setUserSelectedBillingId] = useState<string>('');
  const [couponCode, setCouponCode] = useState<string>('');

  const defaultShippingId = addresses?.find((a: any) => a.isDefaultShipping)?.id || addresses?.[0]?.id || '';
  const defaultBillingId = addresses?.find((a: any) => a.isDefaultBilling)?.id || addresses?.[0]?.id || '';

  const selectedShippingId = userSelectedShippingId || defaultShippingId;
  const selectedBillingId = userSelectedBillingId || defaultBillingId;

  const { data: quote, isFetching: isQuoting } = useCheckoutQuoteQuery({
    shippingAddressId: selectedShippingId,
    billingAddressId: selectedBillingId,
    couponCode: couponCode || undefined
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!selectedShippingId || !selectedBillingId) {
      toast.error('Delivery coordinates missing. Please select your addresses.');
      return;
    }

    submitCheckout(
      {
        shippingAddressId: selectedShippingId,
        billingAddressId: selectedBillingId,
        couponCode: couponCode || undefined,
        paymentMethod: {
          provider: 'iyzico',
          methodType: 'CREDIT_CARD',
          paymentToken: null,
          useThreeDSecure: false
        }
      },
      {
        onSuccess: (response) => {
          if (!response.redirectUrl) {
            toast.success('Transaction secured. Order successfully placed.');
            setTimeout(() => navigate('/orders'), 2000);
          } else {
             // Handle 3D Secure redirect if implemented
             window.location.href = response.redirectUrl;
          }
        },
        onError: (error: any) => {
          toast.error(errorMessage(error) || 'Transaction failed. Please verify your details.');
        },
      }
    );
  };

  const isInitialLoading = isBasketLoading || isAddressesLoading;
  const isPending = isQuoting || isSubmitting;

  if (isInitialLoading) return (
    <div className="flex flex-col min-h-[60vh] items-center justify-center gap-4">
      <Spinner size="lg" />
      <span className="text-[10px] font-black uppercase tracking-[0.4em] text-muted-foreground animate-pulse">Initializing Secure Channel...</span>
    </div>
  );

  if (!basket?.items?.length) return (
    <div className="max-w-xl mx-auto py-32 text-center px-6 bg-card border border-border rounded-[3rem] shadow-2xl mt-12">
       <div className="bg-primary/10 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-8">
          <PackageCheck className="text-primary w-12 h-12 opacity-40" />
       </div>
       <h2 className="text-3xl font-black text-foreground uppercase tracking-tight leading-none">Your bag is empty</h2>
       <p className="text-muted-foreground font-medium mt-4 mb-10 max-w-xs mx-auto">Add some premium products to your bag before proceeding to secure checkout.</p>
       <Button onClick={() => navigate('/')} variant="primary" className="rounded-2xl px-12 h-14 font-black uppercase tracking-widest text-xs">
         Return to Catalog
       </Button>
    </div>
  );

  return (
    <div className="mx-auto max-w-7xl px-4 py-16">
      <header className="mb-16 flex flex-col md:flex-row md:items-end justify-between gap-8">
         <div className="space-y-4">
            <button onClick={() => navigate('/basket')} className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-muted-foreground hover:text-primary transition-colors">
               <ArrowLeft size={14} /> Back to Bag
            </button>
            <h1 className="text-6xl font-black text-foreground tracking-tighter uppercase leading-none">Secure Checkout</h1>
            <div className="flex items-center gap-3 text-emerald-600 font-bold text-[10px] uppercase tracking-widest bg-emerald-500/10 px-4 py-2 rounded-full border border-emerald-500/20 w-fit">
               <ShieldCheck size={14} />
               <span>Tier-1 AES-256 Encrypted Session</span>
            </div>
         </div>
         
         <div className="hidden lg:flex items-center gap-8 text-muted-foreground">
            <div className="text-right">
               <p className="text-[10px] font-black uppercase tracking-widest opacity-40">Session ID</p>
               <p className="text-xs font-mono font-bold text-foreground">SX-{Math.random().toString(36).substring(7).toUpperCase()}</p>
            </div>
            <div className="h-10 w-px bg-border" />
            <div className="flex items-center gap-3">
               <Clock className="text-primary" size={20} />
               <div>
                  <p className="text-[10px] font-black uppercase tracking-widest opacity-40">Expires in</p>
                  <p className="text-xs font-bold text-foreground">14:59</p>
               </div>
            </div>
         </div>
      </header>

      <div className="grid lg:grid-cols-12 gap-16 items-start">
        {/* Main Form Area */}
        <div className="lg:col-span-7 space-y-12">
          
          {/* Shipping Section */}
          <section className="bg-card rounded-[2.5rem] p-10 border border-border shadow-2xl shadow-primary/5 transition-all hover:shadow-primary/10">
             <div className="flex items-center justify-between mb-10">
                <div className="flex items-center gap-6">
                   <div className="w-12 h-12 bg-primary text-primary-foreground rounded-2xl flex items-center justify-center font-black text-xl shadow-xl shadow-primary/20">1</div>
                   <div className="space-y-1">
                      <h2 className="text-2xl font-black text-foreground uppercase tracking-tight">Shipping</h2>
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Select destination</p>
                   </div>
                </div>
                <Button onClick={() => navigate('/profile')} variant="ghost" className="text-[10px] font-black uppercase tracking-widest">+ New Address</Button>
             </div>
             
             <div className="grid gap-5">
                {addresses?.map((addr: any) => (
                  <AddressCard 
                    key={addr.id}
                    addr={addr}
                    selected={selectedShippingId === addr.id}
                    onSelect={() => setUserSelectedShippingId(addr.id)}
                  />
                ))}
                {!addresses?.length ? (
                  <div className="p-12 border-2 border-dashed border-border rounded-[2rem] text-center bg-muted/20">
                    <MapPin className="mx-auto text-muted-foreground/30 mb-6" size={48} />
                    <p className="text-muted-foreground font-black uppercase tracking-widest text-[10px]">No delivery profiles found</p>
                    <Button variant="primary" className="mt-6 rounded-xl px-8" onClick={() => navigate('/profile')}>Create Profile</Button>
                  </div>
                ) : null}
             </div>
          </section>

          {/* Billing Section */}
          <section className="bg-card rounded-[2.5rem] p-10 border border-border shadow-2xl shadow-primary/5 transition-all hover:shadow-primary/10">
             <div className="flex items-center gap-6 mb-10">
                <div className="w-12 h-12 bg-primary text-primary-foreground rounded-2xl flex items-center justify-center font-black text-xl shadow-xl shadow-primary/20">2</div>
                <div className="space-y-1">
                   <h2 className="text-2xl font-black text-foreground uppercase tracking-tight">Billing</h2>
                   <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Select invoice profile</p>
                </div>
             </div>
             
             <div className="grid gap-6">
                <div className="flex p-1 bg-muted rounded-2xl border border-border">
                   <button 
                    onClick={() => setUserSelectedBillingId(selectedShippingId)}
                    className={`flex-1 py-4 px-6 rounded-xl font-black text-[10px] uppercase tracking-widest transition-all ${selectedBillingId === selectedShippingId ? 'bg-card text-primary shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}
                   >
                     Match Shipping
                   </button>
                   <button 
                    onClick={() => setUserSelectedBillingId('')}
                    className={`flex-1 py-4 px-6 rounded-xl font-black text-[10px] uppercase tracking-widest transition-all ${selectedBillingId !== selectedShippingId ? 'bg-card text-primary shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}
                   >
                     Different Address
                   </button>
                </div>

                {selectedBillingId !== selectedShippingId ? (
                  <div className="grid gap-4 animate-in fade-in slide-in-from-top-4 duration-500">
                    {addresses?.map((addr: any) => (
                      <AddressCard 
                        key={addr.id}
                        addr={addr}
                        selected={selectedBillingId === addr.id}
                        onSelect={() => setUserSelectedBillingId(addr.id)}
                      />
                    ))}
                  </div>
                ) : null}
             </div>
          </section>

          {/* Logistics Note */}
          <div className="bg-primary/5 border border-primary/10 rounded-[2rem] p-6 flex items-start gap-4">
             <PackageCheck className="text-primary shrink-0" size={24} />
             <div className="space-y-1">
                <p className="text-sm font-black text-foreground">Standard Premium Logistics</p>
                <p className="text-xs font-medium text-muted-foreground leading-relaxed">Your order will be dispatched within 24 hours. Transit time is estimated at 2-3 business days. All items are insured up to their full value.</p>
             </div>
          </div>
        </div>

        {/* Sidebar Summary */}
        <div className="lg:col-span-5 lg:sticky lg:top-24">
           <div className="bg-gray-950 text-white rounded-[3rem] p-10 lg:p-12 shadow-2xl relative overflow-hidden border border-white/5">
              <div className="absolute top-0 right-0 w-80 h-80 bg-primary/10 rounded-full blur-[100px] -mr-40 -mt-40" />
              
              <div className="relative z-10 space-y-10">
                <header className="flex items-center justify-between">
                   <h2 className="text-3xl font-black uppercase tracking-tighter text-white/90">Review</h2>
                   <div className="bg-white/10 px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest border border-white/5">
                     {basket.items.length} Product{basket.items.length > 1 ? 's' : ''}
                   </div>
                </header>

                <div className="space-y-6 max-h-[35vh] overflow-y-auto pr-4 custom-scrollbar">
                   {basket.items.map((item: any) => (
                     <div key={item.id} className="flex gap-5 group">
                        <div className="w-20 h-20 bg-white/5 rounded-[1.5rem] flex-shrink-0 flex items-center justify-center p-3 border border-white/5 group-hover:bg-white/10 transition-all duration-500">
                           {item.imageUrl ? (
                             <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover rounded-lg" />
                           ) : (
                             <PackageCheck size={28} className="text-white/20" />
                           )}
                        </div>
                        <div className="flex-1 flex flex-col justify-center gap-1">
                           <div className="text-xs font-black uppercase tracking-widest line-clamp-1 text-white/80 group-hover:text-white transition-colors">{item.name}</div>
                           <div className="flex items-center gap-3 text-[10px] font-bold text-white/40 uppercase tracking-widest mt-1">
                             <span>QTY: {item.quantity}</span>
                             <span className="w-1 h-1 bg-white/10 rounded-full" />
                             <span>{formatTRY(item.price)}</span>
                           </div>
                        </div>
                        <div className="text-sm font-black flex items-center tabular-nums text-white/90">
                           {formatTRY(item.price * item.quantity)}
                        </div>
                     </div>
                   ))}
                </div>

                <div className="h-px bg-white/5 w-full" />

                <div className="space-y-6">
                   <div className="flex justify-between items-center text-white/40 font-bold uppercase tracking-[0.2em] text-[10px]">
                      <span>Bag Value</span>
                      <span className="text-white/80 tabular-nums font-black">{formatTRY(quote?.money?.subtotalAmount || 0)}</span>
                   </div>
                   {quote?.money?.discountAmount > 0 ? (
                     <div className="flex justify-between items-center text-emerald-400 font-bold uppercase tracking-[0.2em] text-[10px]">
                        <span>Privilege Discount</span>
                        <span className="font-black tabular-nums">-{formatTRY(quote.money.discountAmount)}</span>
                     </div>
                   ) : null}
                   <div className="flex justify-between items-center text-white/40 font-bold uppercase tracking-[0.2em] text-[10px]">
                      <span>Logistics</span>
                      <span className="text-emerald-400 font-black">Free</span>
                   </div>
                   
                   <div className="pt-4 flex justify-between items-end">
                      <div className="space-y-2">
                         <span className="text-[10px] font-black uppercase tracking-[0.4em] text-white/20">Final Total</span>
                         <p className="text-5xl font-black tracking-tighter leading-none text-primary tabular-nums">
                           {formatTRY(quote?.money?.grandTotalAmount || 0)}
                         </p>
                      </div>
                      {isQuoting ? <Spinner size="sm" className="text-primary mb-3" /> : null}
                   </div>
                </div>

                <div className="pt-8 space-y-5">
                   {/* Promo Code Input In Summary */}
                   <div className="flex gap-2 p-1.5 bg-white/5 rounded-2xl border border-white/5">
                      <input 
                        type="text" 
                        placeholder="COUPON?" 
                        className="flex-1 bg-transparent border-none outline-none px-4 font-black uppercase tracking-widest text-[10px] placeholder:text-white/20"
                        value={couponCode}
                        onChange={(e) => setCouponCode(e.target.value)}
                      />
                      <button className="bg-white/10 hover:bg-white/20 px-6 py-3 rounded-xl text-[9px] font-black uppercase tracking-widest transition-all">Apply</button>
                   </div>

                   <Button
                    onClick={handleSubmit}
                    disabled={isPending || !quote}
                    className="w-full h-20 rounded-[1.5rem] bg-primary hover:bg-primary/90 text-primary-foreground font-black text-xl uppercase tracking-widest shadow-3xl shadow-primary/40 border-none transition-all active:scale-[0.97]"
                   >
                    {isSubmitting ? (
                      <div className="flex items-center gap-4">
                         <Spinner size="sm" className="text-white" />
                         <span>Processing...</span>
                      </div>
                    ) : (
                      <div className="flex items-center gap-4">
                         <CreditCard />
                         <span>Pay Securely</span>
                      </div>
                    )}
                   </Button>
                   
                   <p className="text-center text-white/20 text-[9px] font-black uppercase tracking-[0.3em]">
                      Securely processed by Iyzico Systems
                   </p>
                </div>
              </div>
           </div>
        </div>
      </div>
    </div>
  );
};

interface AddressCardProps {
  addr: any;
  selected: boolean;
  onSelect: () => void;
}

const AddressCard: React.FC<AddressCardProps> = ({ addr, selected, onSelect }) => {
  return (
    <button 
      type="button"
      onClick={onSelect}
      className={`
        w-full text-left p-6 rounded-[2rem] border-2 transition-all group relative overflow-hidden
        ${selected 
          ? 'bg-primary/5 border-primary shadow-xl shadow-primary/5' 
          : 'bg-card border-border hover:border-primary/20 hover:bg-muted/30'
        }
      `}
    >
      {selected && (
        <div className="absolute top-0 right-0 p-4 animate-in zoom-in duration-300">
           <CheckCircle2 size={24} className="text-primary" />
        </div>
      )}
      
      <div className="flex items-start gap-4">
        <div className={`p-3 rounded-xl transition-colors ${selected ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground group-hover:text-primary'}`}>
           <MapPin size={20} />
        </div>
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            <span className={`text-sm font-black uppercase tracking-widest ${selected ? 'text-primary' : 'text-foreground'}`}>
              {addr.title}
            </span>
            {addr.isDefaultShipping ? (
               <span className="text-[8px] font-black uppercase tracking-widest bg-blue-500/10 text-blue-500 px-2 py-0.5 rounded-full border border-blue-500/10">Default</span>
            ) : null}
          </div>
          <p className="text-sm font-bold text-muted-foreground">{addr.addressLine1}</p>
          <p className="text-xs font-medium text-muted-foreground/60">{addr.city}, {addr.country}</p>
        </div>
      </div>
    </button>
  );
};

export default CheckoutPage;
