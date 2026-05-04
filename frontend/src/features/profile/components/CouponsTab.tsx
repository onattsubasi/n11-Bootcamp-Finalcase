import React from 'react';
import { useCoupons } from '../hooks/useUserHooks';
import { Spinner } from '@/components/ui/Spinner';
import { Tag, Ticket, Clock, CheckCircle2, ChevronRight, Copy } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { formatDate } from '@/lib/utils/format';

const CouponsTab: React.FC = () => {
  const { data, isPending, isError } = useCoupons();

  const copyToClipboard = (code: string) => {
    navigator.clipboard.writeText(code);
    toast.success('Code copied to clipboard.');
  };

  if (isPending) return (
    <div className="flex justify-center py-32">
      <Spinner size="lg" />
    </div>
  );

  if (isError) return (
    <div className="p-12 bg-destructive/5 border border-destructive/20 rounded-[3rem] text-destructive text-center">
       <p className="text-[10px] font-black uppercase tracking-widest">Coupon Protocol Error</p>
    </div>
  );

  const coupons = data?.content || [];

  if (coupons.length === 0) {
    return (
      <div className="py-24 text-center bg-muted/20 rounded-[3rem] border-2 border-dashed border-border group transition-all hover:border-primary/20">
         <div className="bg-muted rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
            <Ticket className="text-muted-foreground/20" size={32} />
         </div>
         <h3 className="text-xl font-black text-foreground uppercase tracking-tight">No active privileges</h3>
         <p className="text-muted-foreground font-medium mt-2 mb-8 max-w-xs mx-auto text-sm">Exclusive promotional codes will appear here as they are issued to your account.</p>
      </div>
    );
  }

  return (
    <div className="space-y-10 animate-in fade-in slide-in-from-bottom-8 duration-700">
      <header className="flex items-center justify-between border-b border-border pb-6">
         <div className="flex items-center gap-3">
            <Tag className="text-primary" size={18} />
            <h2 className="text-[10px] font-black uppercase tracking-[0.3em] text-foreground">Active Privileges</h2>
         </div>
         <span className="text-[10px] font-black text-muted-foreground uppercase tracking-widest">{coupons.length} Coupons Available</span>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {coupons.map((coupon: any) => (
          <div key={coupon.id} className="group relative overflow-hidden bg-card border border-border rounded-[2.5rem] p-8 hover:border-primary/20 hover:shadow-2xl hover:shadow-primary/5 transition-all duration-500">
             <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full blur-3xl -mr-16 -mt-16 group-hover:bg-primary/10 transition-colors" />
             
             <div className="relative z-10 flex flex-col gap-6">
                <div className="flex items-center justify-between">
                   <div className="bg-primary/10 text-primary p-3 rounded-2xl border border-primary/20">
                      <Ticket size={24} />
                   </div>
                   <div className="flex items-center gap-2 bg-emerald-500/10 text-emerald-500 text-[8px] font-black px-2.5 py-1 rounded-full border border-emerald-500/10 uppercase tracking-widest">
                      <CheckCircle2 size={10} /> Active
                   </div>
                </div>

                <div className="space-y-1">
                   <h3 className="text-2xl font-black text-foreground uppercase tracking-tighter leading-tight">{coupon.description || 'Exclusive Discount'}</h3>
                   <div className="flex items-center gap-4 pt-1">
                      <div className="flex items-center gap-2 text-muted-foreground">
                         <Clock size={12} />
                         <span className="text-[10px] font-bold uppercase tracking-widest">Expires {formatDate(coupon.expiryDate)}</span>
                      </div>
                   </div>
                </div>

                <div className="flex items-center gap-2 p-1.5 bg-muted/50 rounded-2xl border border-border group-hover:border-primary/20 transition-colors">
                   <div className="flex-1 px-4 font-mono font-black text-foreground tracking-[0.3em] text-sm py-3">
                      {coupon.code}
                   </div>
                   <button 
                    onClick={() => copyToClipboard(coupon.code)}
                    className="bg-primary text-primary-foreground p-3 rounded-xl hover:scale-105 active:scale-95 transition-all shadow-lg shadow-primary/20"
                   >
                      <Copy size={16} />
                   </button>
                </div>
                
                <div className="flex items-center justify-between pt-2 border-t border-dashed border-border mt-2">
                   <span className="text-[9px] font-black uppercase tracking-widest text-muted-foreground">Minimum Requirement</span>
                   <span className="text-[10px] font-black text-foreground">TRY 500.00</span>
                </div>
             </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CouponsTab;
