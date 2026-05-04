import React, { useState } from 'react';
import { toast } from 'react-hot-toast';
import { 
  useAddresses, 
  useDeleteAddress, 
  useSetDefaultShipping, 
  useSetDefaultBilling 
} from '../hooks/useAddresses';
import AddressForm from './AddressForm';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { 
  MapPin, 
  Plus, 
  Trash2, 
  Edit3, 
  CheckCircle2, 
  CreditCard, 
  Truck,
  MoreVertical,
  ChevronRight
} from 'lucide-react';
import { errorMessage } from '@/api/problem';

export const AddressBook: React.FC = () => {
  const { data, isLoading, isError, error } = useAddresses();
  const deleteMutation = useDeleteAddress();
  const defaultShippingMutation = useSetDefaultShipping();
  const defaultBillingMutation = useSetDefaultBilling();

  const [editingAddress, setEditingAddress] = useState<any>(null);
  const [isAddingNew, setIsAddingNew] = useState(false);

  if (isLoading) return (
    <div className="flex justify-center p-20">
      <Spinner size="lg" />
    </div>
  );

  if (isError) return (
    <div className="p-10 bg-destructive/5 border border-destructive/20 rounded-[2rem] text-destructive text-center">
       <p className="text-[10px] font-black uppercase tracking-widest">Protocol Sync Error</p>
       <p className="text-sm font-medium mt-1">{errorMessage(error) || 'Failed to load logistics database.'}</p>
    </div>
  );

  const addresses = data?.content || data || [];

  const handleAction = async (actionFn: any, addressId: string, successText: string) => {
    try {
      await actionFn.mutateAsync(addressId);
      toast.success(successText);
    } catch (err: any) {
      toast.error(errorMessage(err) || 'Action failed.');
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Permanently decommission this delivery profile?')) {
      handleAction(deleteMutation, id, 'Address decommissioned.');
    }
  };

  const handleFormFinish = () => {
    setEditingAddress(null);
    setIsAddingNew(false);
  };

  return (
    <div className="space-y-10">
      <header className="flex items-center justify-between">
         <div className="space-y-1">
            <h2 className="text-xl font-black text-foreground uppercase tracking-tight">Logistics Profiles</h2>
            <p className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Manage Delivery & Billing Targets</p>
         </div>
         {!isAddingNew && !editingAddress && (
           <Button
             onClick={() => {
               setEditingAddress(null);
               setIsAddingNew(true);
             }}
             className="rounded-2xl h-12 px-6 bg-primary text-primary-foreground font-black text-[10px] uppercase tracking-widest shadow-xl shadow-primary/10 flex items-center gap-2 transition-all active:scale-95"
           >
             <Plus size={16} /> Add New Profile
           </Button>
         )}
      </header>

      {isAddingNew || editingAddress ? (
        <div className="bg-card border border-border rounded-[2.5rem] p-10 shadow-2xl shadow-primary/5 animate-in fade-in slide-in-from-top-8 duration-500">
           <div className="flex items-center gap-4 mb-10">
              <div className="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center text-primary">
                 {isAddingNew ? <Plus size={20} /> : <Edit3 size={20} />}
              </div>
              <h3 className="text-lg font-black uppercase tracking-tight">
                 {isAddingNew ? 'Initialize New Profile' : 'Modify Existing Profile'}
              </h3>
           </div>
           <AddressForm
             initialData={editingAddress}
             onCancel={() => {
               setIsAddingNew(false);
               setEditingAddress(null);
             }}
             onSuccess={handleFormFinish}
           />
        </div>
      ) : null}

      {!isAddingNew && !editingAddress && (
        <>
          {addresses.length === 0 ? (
            <div className="text-center py-24 bg-muted/20 rounded-[3rem] border-2 border-dashed border-border group hover:border-primary/20 transition-all">
              <div className="bg-muted rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
                 <MapPin className="text-muted-foreground/30" size={32} />
              </div>
              <p className="text-muted-foreground font-black uppercase tracking-[0.3em] text-[10px]">No verified delivery targets</p>
              <Button variant="ghost" className="mt-6 text-[10px] font-black uppercase tracking-widest" onClick={() => setIsAddingNew(true)}>
                 + Add Initial Profile
              </Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {addresses.map((addr: any) => (
                <div key={addr.id} className="bg-card border border-border p-8 rounded-[2.5rem] relative group hover:border-primary/20 hover:shadow-2xl hover:shadow-primary/5 transition-all duration-500 overflow-hidden">
                  <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full blur-3xl -mr-16 -mt-16 group-hover:bg-primary/10 transition-colors" />
                  
                  <div className="flex items-center justify-between mb-6 relative z-10">
                    <div className="flex flex-wrap gap-2">
                      {addr.isDefaultShipping && (
                        <div className="flex items-center gap-1.5 bg-emerald-500/10 text-emerald-500 text-[8px] font-black px-2.5 py-1 rounded-full border border-emerald-500/10 uppercase tracking-widest">
                           <Truck size={10} /> Shipping
                        </div>
                      )}
                      {addr.isDefaultBilling && (
                        <div className="flex items-center gap-1.5 bg-blue-500/10 text-blue-500 text-[8px] font-black px-2.5 py-1 rounded-full border border-blue-500/10 uppercase tracking-widest">
                           <CreditCard size={10} /> Billing
                        </div>
                      )}
                    </div>
                    
                    <div className="flex items-center gap-1">
                       <button 
                        onClick={() => setEditingAddress(addr)}
                        className="p-2 text-muted-foreground hover:text-primary transition-colors rounded-lg hover:bg-primary/5"
                       >
                          <Edit3 size={16} />
                       </button>
                       <button 
                        onClick={() => handleDelete(addr.id)}
                        className="p-2 text-muted-foreground hover:text-destructive transition-colors rounded-lg hover:bg-destructive/5"
                       >
                          <Trash2 size={16} />
                       </button>
                    </div>
                  </div>

                  <div className="relative z-10 space-y-4">
                     <div className="space-y-1">
                        <h4 className="font-black text-xl text-foreground uppercase tracking-tight">{addr.title}</h4>
                        <p className="text-xs font-bold text-muted-foreground">{addr.recipientName}</p>
                     </div>
                     
                     <div className="space-y-1.5">
                        <div className="flex items-start gap-3">
                           <MapPin size={14} className="text-primary mt-0.5 shrink-0" />
                           <p className="text-sm font-medium text-muted-foreground leading-relaxed">
                              {addr.addressLine1} {addr.addressLine2 && `, ${addr.addressLine2}`}<br />
                              {addr.city}, {addr.country}
                           </p>
                        </div>
                        <div className="flex items-center gap-3">
                           <CheckCircle2 size={14} className="text-emerald-500 shrink-0" />
                           <p className="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest">
                              {addr.zipCode}
                           </p>
                        </div>
                     </div>
                  </div>

                  <div className="mt-8 pt-6 border-t border-border flex flex-wrap gap-4 relative z-10">
                    {!addr.isDefaultShipping && (
                      <button 
                        onClick={() => handleAction(defaultShippingMutation, addr.id, 'Shipping protocol updated')}
                        className="text-[9px] font-black uppercase tracking-widest text-muted-foreground hover:text-primary transition-colors flex items-center gap-2"
                      >
                         <Truck size={14} /> Set Default Shipping
                      </button>
                    )}
                    {!addr.isDefaultBilling && (
                      <button 
                        onClick={() => handleAction(defaultBillingMutation, addr.id, 'Billing protocol updated')}
                        className="text-[9px] font-black uppercase tracking-widest text-muted-foreground hover:text-primary transition-colors flex items-center gap-2"
                      >
                         <CreditCard size={14} /> Set Default Billing
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AddressBook;
