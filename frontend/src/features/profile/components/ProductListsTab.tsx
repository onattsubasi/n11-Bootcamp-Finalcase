import React, { useState } from 'react';
import { 
  useProductLists, 
  useCreateProductList, 
  useDeleteProductList, 
  useRemoveFromProductList,
  useRenameProductList
} from '../hooks/useUserHooks';
import { Spinner } from '@/components/ui/Spinner';
import { 
  List, 
  Plus, 
  Trash2, 
  Edit3, 
  ChevronRight, 
  Package, 
  MoreHorizontal,
  ExternalLink,
  ShoppingBag,
  FolderOpen,
  ArrowRight
} from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { formatTRY } from '@/lib/utils/format';
import { useAddToCart } from '@/features/basket/hooks/useBasket';

const ProductListsTab: React.FC = () => {
  const navigate = useNavigate();
  const { data: lists, isPending, isError } = useProductLists();
  const createListMutation = useCreateProductList();
  const deleteListMutation = useDeleteProductList();
  const removeFromListMutation = useRemoveFromProductList();
  const renameListMutation = useRenameProductList();
  const { mutate: addToCart } = useAddToCart();

  const [newListName, setNewListName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [editingListId, setEditingListId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [expandedListId, setExpandedListId] = useState<string | null>(null);

  const handleCreateList = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newListName.trim()) return;
    
    createListMutation.mutate(newListName, {
      onSuccess: () => {
        setNewListName('');
        setIsCreating(false);
        toast.success('Collection initialized.');
      }
    });
  };

  const handleRename = (id: string) => {
    if (!editName.trim()) return;
    renameListMutation.mutate({ listId: id, name: editName }, {
      onSuccess: () => {
        setEditingListId(null);
        toast.success('Collection rebranded.');
      }
    });
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Dissolve this collection permanently?')) {
      deleteListMutation.mutate(id, {
        onSuccess: () => toast.success('Collection dissolved.')
      });
    }
  };

  const handleRemoveItem = (listId: string, productId: string) => {
    removeFromListMutation.mutate({ listId, productId }, {
      onSuccess: () => toast.success('Item removed from collection.')
    });
  };

  const handleAddToBasket = (productId: string) => {
    addToCart({ productId, quantity: 1 }, {
      onSuccess: () => toast.success('Item added to secure bag.')
    });
  };

  if (isPending) return (
    <div className="flex justify-center py-32">
      <Spinner size="lg" />
    </div>
  );

  if (isError) return (
    <div className="p-12 bg-destructive/5 border border-destructive/20 rounded-[3rem] text-destructive text-center">
       <p className="text-[10px] font-black uppercase tracking-widest">Collection Protocol Error</p>
    </div>
  );

  return (
    <div className="space-y-12 animate-in fade-in slide-in-from-bottom-8 duration-700">
      <header className="flex items-center justify-between border-b border-border pb-6">
         <div className="flex items-center gap-3">
            <List className="text-primary" size={18} />
            <h2 className="text-[10px] font-black uppercase tracking-[0.3em] text-foreground">Curated Collections</h2>
         </div>
         {!isCreating && (
           <Button
             onClick={() => setIsCreating(true)}
             className="rounded-2xl h-12 px-6 bg-primary text-primary-foreground font-black text-[10px] uppercase tracking-widest flex items-center gap-2"
           >
             <Plus size={16} /> New Collection
           </Button>
         )}
      </header>

      {isCreating && (
        <form onSubmit={handleCreateList} className="bg-card border border-border rounded-[2.5rem] p-10 shadow-2xl shadow-primary/5 flex flex-col sm:flex-row gap-6 animate-in zoom-in-95 duration-500">
           <div className="flex-1 space-y-2">
              <label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground ml-1">Collection Identity</label>
              <Input
                autoFocus
                className="rounded-2xl h-14 bg-muted/30 border-border focus:bg-card transition-all font-bold px-6"
                placeholder="e.g. Summer Essentials 2024"
                value={newListName}
                onChange={(e) => setNewListName(e.target.value)}
              />
           </div>
           <div className="flex items-end gap-3">
              <Button type="submit" disabled={createListMutation.isPending} className="h-14 px-8 rounded-2xl bg-primary text-primary-foreground font-black uppercase tracking-widest text-[10px]">Initialize</Button>
              <Button variant="ghost" className="h-14 px-6 rounded-2xl text-[10px] font-black uppercase tracking-widest" onClick={() => setIsCreating(false)}>Cancel</Button>
           </div>
        </form>
      )}

      <div className="grid gap-10">
        {lists?.length === 0 ? (
          <div className="py-24 text-center bg-muted/20 rounded-[3rem] border-2 border-dashed border-border group transition-all hover:border-primary/20">
             <div className="bg-muted rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
                <FolderOpen className="text-muted-foreground/20" size={32} />
             </div>
             <h3 className="text-xl font-black text-foreground uppercase tracking-tight">No collections found</h3>
             <p className="text-muted-foreground font-medium mt-2 mb-8 max-w-xs mx-auto text-sm">Organize your shopping by creating custom collections of premium items.</p>
          </div>
        ) : (
          lists?.map((list: any) => (
            <div key={list.id} className="group bg-card border border-border rounded-[3rem] overflow-hidden shadow-2xl shadow-primary/5 transition-all duration-500 hover:shadow-primary/10">
               <div className="p-10">
                  <div className="flex items-center justify-between mb-8">
                     <div className="flex items-center gap-6">
                        <div className="w-16 h-16 bg-primary/10 rounded-[1.5rem] flex items-center justify-center text-primary group-hover:scale-110 transition-transform duration-500">
                           <List size={28} />
                        </div>
                        <div className="space-y-1">
                           {editingListId === list.id ? (
                             <div className="flex items-center gap-3">
                                <Input 
                                  className="h-10 rounded-xl font-bold bg-muted/50" 
                                  value={editName} 
                                  onChange={(e) => setEditName(e.target.value)}
                                  autoFocus
                                />
                                <button onClick={() => handleRename(list.id)} className="text-emerald-500"><Plus size={20} /></button>
                                <button onClick={() => setEditingListId(null)} className="text-muted-foreground"><Plus size={20} className="rotate-45" /></button>
                             </div>
                           ) : (
                             <div className="flex items-center gap-4">
                                <h3 className="text-2xl font-black text-foreground uppercase tracking-tighter leading-none">{list.name}</h3>
                                <button 
                                  onClick={() => { setEditingListId(list.id); setEditName(list.name); }}
                                  className="p-1.5 text-muted-foreground hover:text-primary transition-colors opacity-0 group-hover:opacity-100"
                                >
                                   <Edit3 size={14} />
                                </button>
                             </div>
                           )}
                           <p className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">{list.items?.length || 0} Products Cataloged</p>
                        </div>
                     </div>

                     <div className="flex items-center gap-4">
                        <button 
                          onClick={() => setExpandedListId(expandedListId === list.id ? null : list.id)}
                          className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-all ${expandedListId === list.id ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:bg-primary/10 hover:text-primary'}`}
                        >
                           <ChevronRight size={20} className={`transition-transform duration-500 ${expandedListId === list.id ? 'rotate-90' : ''}`} />
                        </button>
                        <button 
                          onClick={() => handleDelete(list.id)}
                          className="w-12 h-12 rounded-2xl bg-muted/50 text-muted-foreground hover:bg-destructive/10 hover:text-destructive flex items-center justify-center transition-all"
                        >
                           <Trash2 size={20} />
                        </button>
                     </div>
                  </div>

                  {expandedListId === list.id && (
                    <div className="animate-in slide-in-from-top-4 fade-in duration-500">
                       <div className="h-px bg-border w-full my-10" />
                       
                       {list.items?.length === 0 ? (
                         <div className="text-center py-12 text-muted-foreground italic text-sm">
                            No items in this collection.
                         </div>
                       ) : (
                         <div className="grid gap-6">
                            {list.items.map((item: any) => (
                              <div key={item.id} className="flex flex-col sm:flex-row items-center justify-between p-6 bg-muted/20 rounded-[2rem] border border-border/50 group/item hover:bg-muted/40 transition-colors gap-6">
                                 <div className="flex flex-col sm:flex-row items-center gap-6 text-center sm:text-left">
                                    <div className="w-20 h-20 bg-card rounded-[1.5rem] flex items-center justify-center p-3 border border-border group-hover/item:scale-105 transition-transform duration-500 overflow-hidden">
                                       {item.imageUrl ? (
                                         <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover rounded-lg" />
                                       ) : (
                                         <Package size={24} className="text-muted-foreground opacity-20" />
                                       )}
                                    </div>
                                    <div className="space-y-1">
                                       <h4 className="font-black text-foreground uppercase tracking-tight">{item.name}</h4>
                                       <p className="text-[10px] font-black text-primary uppercase tracking-widest">{formatTRY(item.price)}</p>
                                    </div>
                                 </div>
                                 <div className="flex items-center gap-3">
                                    <Button 
                                      onClick={() => navigate(`/product/${item.id}`)}
                                      variant="ghost" 
                                      className="rounded-xl h-12 px-6 text-[10px] font-black uppercase tracking-widest border border-border hover:bg-card"
                                    >
                                       View Product
                                    </Button>
                                    <Button 
                                      onClick={() => handleAddToBasket(item.id)}
                                      className="rounded-xl h-12 px-6 bg-primary text-primary-foreground font-black text-[10px] uppercase tracking-widest shadow-lg shadow-primary/10 flex items-center gap-2"
                                    >
                                       <ShoppingBag size={14} /> Add
                                    </Button>
                                    <button 
                                      onClick={() => handleRemoveItem(list.id, item.id)}
                                      className="p-3 text-muted-foreground hover:text-destructive transition-colors"
                                    >
                                       <Trash2 size={18} />
                                    </button>
                                 </div>
                              </div>
                            ))}
                         </div>
                       )}
                    </div>
                  )}
               </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ProductListsTab;
