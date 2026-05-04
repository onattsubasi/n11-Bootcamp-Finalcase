import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ShoppingCart, 
  Heart, 
  ListPlus, 
  ShieldCheck, 
  Truck, 
  RefreshCw,
  ArrowLeft,
  Share2,
  Bookmark,
  AlertCircle,
  Activity,
  ArrowRight
} from 'lucide-react';
import { useProduct } from '@/features/catalog/hooks/useProduct';
import { useAddToCart } from '@/features/basket/hooks/useBasket';
import { 
  useFavorites, 
  useAddFavorite, 
  useRemoveFavorite,
  useProductLists,
  useAddProductListItem
} from '@/features/profile/hooks/useUserHooks';
import { Button } from '@/components/ui/Button';
import { RatingStars } from '@/components/ui/RatingStars';
import { Spinner } from '@/components/ui/Spinner';
import { formatCurrency } from '@/lib/utils/format';
import { ReviewList } from '@/features/reviews/components/ReviewList';
import { useStore } from '@/store';
import toast from 'react-hot-toast';

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const { data: product, isLoading, isError } = useProduct(id || '');
  const { mutate: addToCart, isPending: isAdding } = useAddToCart();
  
  // Favorites
  const { data: favorites } = useFavorites(0, 100);
  const addFavoriteMutation = useAddFavorite();
  const removeFavoriteMutation = useRemoveFavorite();
  const isFavorite = favorites?.content?.some((f: any) => f.productId === id) || favorites?.items?.some((f: any) => f.productId === id);

  // Lists
  const { data: lists } = useProductLists();
  const addToListMutation = useAddProductListItem();
  const [showListSelector, setShowListSelector] = useState(false);

  if (isLoading) return (
    <div className="flex flex-col min-h-[60vh] items-center justify-center gap-4">
      <Spinner size="lg" />
      <span className="text-[10px] font-black uppercase tracking-[0.4em] text-muted-foreground animate-pulse">Decompressing Product Data...</span>
    </div>
  );

  if (isError) return (
    <div className="max-w-xl mx-auto my-24 p-12 bg-card border border-destructive/20 rounded-[3rem] text-center shadow-2xl">
      <div className="bg-destructive/10 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-8">
         <AlertCircle className="text-destructive w-10 h-10 opacity-40" />
      </div>
      <h2 className="text-2xl font-black text-foreground uppercase tracking-tight leading-none">Catalog Sync Failure</h2>
      <p className="text-muted-foreground font-medium mt-4 mb-10 max-w-xs mx-auto">We could not retrieve the target SKU details. It may have been decommissioned or moved.</p>
      <Button onClick={() => navigate('/')} variant="primary" className="rounded-2xl px-12 h-14 font-black uppercase tracking-widest text-xs">Return to Catalog</Button>
    </div>
  );
  
  if (!product) return null;

  const productId = product.productId ?? product.id ?? id ?? '';
  const currentPrice = product.discountedPrice ?? product.effectivePrice ?? product.price ?? product.basePrice ?? 0;
  const originalPrice = product.price ?? product.basePrice ?? currentPrice;
  const formatTRY = (amount?: number) => formatCurrency(amount ?? 0, product.currency ?? 'TRY');

  const handleAddToBasket = () => {
    if (!isAuthenticated) {
      toast.error('Authentication required for checkout operations.');
      return;
    }

    addToCart(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Product secured in bag.'),
        onError: (err: any) => toast.error(err.message || 'Transaction interrupted.'),
      }
    );
  };

  const toggleFavorite = () => {
    if (!isAuthenticated) return toast.error('Authentication required for favorites.');
    
    if (isFavorite) {
      removeFavoriteMutation.mutate(productId, { onSuccess: () => toast.success('Removed from wishlist.') });
    } else {
      addFavoriteMutation.mutate(productId, { onSuccess: () => toast.success('Saved to wishlist.') });
    }
  };

  const addToList = (listId: string) => {
    addToListMutation.mutate({ listId, productId, note: '' }, {
      onSuccess: () => {
        toast.success('Archived in collection.');
        setShowListSelector(false);
      }
    });
  };

  const isOutOfStock = (product.stockQuantity ?? 0) === 0;

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 space-y-16 animate-in fade-in duration-1000">
      <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-muted-foreground hover:text-primary transition-colors">
         <ArrowLeft size={14} /> Navigate Back
      </button>

      <section className="bg-card rounded-[3.5rem] shadow-3xl shadow-primary/5 border border-border overflow-hidden relative">
        <div className="grid lg:grid-cols-2">
          {/* Media Engine */}
          <div className="bg-muted/30 flex items-center justify-center relative p-12 lg:p-20 group border-r border-border">
            <div className="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent pointer-events-none" />
            
            <div className="w-full aspect-square bg-card rounded-[3rem] shadow-2xl overflow-hidden flex items-center justify-center p-12 border border-border group-hover:scale-[1.02] transition-all duration-1000 relative">
               <div className="absolute inset-0 bg-gradient-to-tr from-primary/5 via-transparent to-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-1000" />
               {product.imageUrl ? (
                 <img 
                   src={product.imageUrl} 
                   alt={product.name} 
                   className="w-full h-full object-contain mix-blend-multiply transition-transform duration-1000 group-hover:scale-110" 
                 />
               ) : (
                 <div className="flex flex-col items-center gap-4 text-muted-foreground/20">
                    <ShoppingCart size={100} strokeWidth={1} />
                    <span className="font-black uppercase tracking-[0.5em] text-xs">SKU Preview N/A</span>
                 </div>
               )}
            </div>
            
            <div className="absolute top-10 left-10 flex flex-col gap-3">
               {(product.discountPercentage ?? 0) > 0 && (
                 <div className="bg-destructive text-destructive-foreground px-4 py-2 rounded-2xl font-black text-xs uppercase tracking-widest shadow-xl shadow-destructive/20 border-none">
                    -{product.discountPercentage}%
                 </div>
               )}
               <div className="bg-gray-950 text-white px-4 py-2 rounded-2xl font-black text-[9px] uppercase tracking-widest shadow-xl border border-white/10">
                  REF: {productId.substring(0, 8).toUpperCase()}
               </div>
            </div>

            <div className="absolute top-10 right-10 flex flex-col gap-3">
               <button className="bg-card/80 backdrop-blur-md p-3 rounded-2xl border border-border hover:bg-card hover:scale-110 transition-all text-muted-foreground hover:text-primary shadow-xl shadow-primary/5">
                  <Share2 size={18} />
               </button>
               <button 
                onClick={toggleFavorite}
                className={`p-3 rounded-2xl border transition-all shadow-xl shadow-primary/5 ${isFavorite ? 'bg-primary text-primary-foreground border-primary' : 'bg-card/80 backdrop-blur-md border-border text-muted-foreground hover:text-primary'}`}
               >
                  <Heart size={18} className={isFavorite ? 'fill-current' : ''} />
               </button>
            </div>
          </div>

          {/* Configuration & Intel */}
          <div className="p-10 lg:p-20 flex flex-col justify-center space-y-10 relative">
            <div className="space-y-6">
              <div className="flex flex-wrap items-center gap-4">
                {product.brand && (
                  <span className="text-[9px] font-black uppercase tracking-[0.3em] text-primary bg-primary/10 px-4 py-1.5 rounded-full border border-primary/10">
                     {product.brand}
                  </span>
                )}
                {isOutOfStock ? (
                  <span className="text-[9px] font-black uppercase tracking-[0.3em] bg-destructive/10 text-destructive px-4 py-1.5 rounded-full border border-destructive/10">OUT OF STOCK</span>
                ) : (
                  <span className="text-[9px] font-black uppercase tracking-[0.3em] bg-emerald-500/10 text-emerald-600 px-4 py-1.5 rounded-full border border-emerald-500/10 flex items-center gap-2">
                     <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                     {product.stockQuantity} UNITS AVAILABLE
                  </span>
                )}
              </div>

              <h1 className="text-5xl lg:text-6xl font-black tracking-tighter text-foreground uppercase leading-none">
                {product.name}
              </h1>

              <div className="flex items-center gap-6 py-4 border-y border-border w-fit px-4 rounded-[1.5rem] bg-muted/20">
                <div className="flex items-center gap-2">
                  <RatingStars value={product.rating ?? 0} size={18} />
                  <span className="text-xl font-black text-foreground tabular-nums">{(product.rating ?? 0).toFixed(1)}</span>
                </div>
                <div className="h-6 w-px bg-border" />
                <span className="text-[10px] font-black text-muted-foreground uppercase tracking-widest leading-none">
                   {product.reviewCount ?? 0} Community Intel reports
                </span>
              </div>
            </div>

            <div className="space-y-6">
              <div className="bg-gray-950 text-white p-10 rounded-[3rem] shadow-3xl shadow-primary/10 inline-block relative overflow-hidden group">
                 <div className="absolute top-0 right-0 w-32 h-32 bg-primary/20 rounded-full blur-3xl -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-1000" />
                 <div className="relative z-10 flex flex-col gap-2">
                    <span className="text-[10px] font-black uppercase tracking-[0.4em] text-white/20">Final Valuation</span>
                    <div className="flex items-baseline gap-4">
                       <span className="text-6xl font-black tracking-tighter text-primary tabular-nums">
                         {formatTRY(currentPrice)}
                       </span>
                       {(product.discountPercentage ?? 0) > 0 && (
                        <span className="text-xl text-white/20 line-through font-bold decoration-primary/30">
                          {formatTRY(originalPrice)}
                        </span>
                      )}
                    </div>
                 </div>
              </div>
              
              <div className="space-y-4 max-w-xl">
                 <p className="text-lg leading-relaxed text-muted-foreground font-medium">
                   {product.description || "Architected for superior performance and aesthetic distinction. This SKU represents our commitment to premium industrial design and user-centric functionality."}
                 </p>
              </div>
            </div>

            {/* Tactical Actions */}
            <div className="flex flex-wrap gap-5 pt-4">
              <Button
                onClick={handleAddToBasket}
                disabled={isAdding || isOutOfStock}
                className="flex-1 min-h-[80px] rounded-[2rem] text-xl font-black uppercase tracking-[0.2em] bg-primary hover:bg-primary/90 text-primary-foreground shadow-3xl shadow-primary/20 transition-all active:scale-[0.98] border-none"
              >
                {isAdding ? (
                   <Spinner size="sm" className="text-white" />
                ) : (
                   <div className="flex items-center gap-4">
                      <ShoppingCart size={24} />
                      <span>{isOutOfStock ? 'Sold Out' : 'Add to Bag'}</span>
                   </div>
                )}
              </Button>
              
              <div className="relative group/list">
                <Button
                  variant="outline"
                  className="w-20 h-20 p-0 rounded-[2rem] flex items-center justify-center border-2 border-border text-muted-foreground hover:text-primary hover:bg-primary/5 transition-all active:scale-95"
                  onClick={() => setShowListSelector(!showListSelector)}
                >
                  <ListPlus size={28} />
                </Button>
                
                {showListSelector && (
                  <div className="absolute right-0 bottom-full mb-6 w-72 bg-card border border-border rounded-[2.5rem] shadow-3xl z-30 overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-500">
                    <div className="p-6 bg-muted/50 border-b border-border text-[9px] font-black text-muted-foreground uppercase tracking-[0.3em] flex items-center gap-3">
                       <Bookmark size={14} className="text-primary" /> Save to Collection
                    </div>
                    <div className="p-3 space-y-1">
                      {lists?.map((list: any) => (
                        <button 
                          key={list.id}
                          onClick={() => addToList(list.id)}
                          className="w-full text-left px-5 py-4 text-[10px] font-black uppercase tracking-widest text-foreground hover:bg-primary hover:text-primary-foreground rounded-2xl transition-all flex items-center justify-between group/item"
                        >
                          {list.name}
                          <ArrowRight size={14} className="opacity-0 group-hover/item:opacity-100 transition-opacity" />
                        </button>
                      ))}
                      {!lists?.length && (
                        <div className="p-8 text-center text-[10px] font-black text-muted-foreground uppercase tracking-widest italic">No targets defined</div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Service Protocols */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 pt-10 border-t border-border">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 bg-primary/5 rounded-xl flex items-center justify-center text-primary">
                   <Truck size={20} />
                </div>
                <span className="text-[9px] font-black uppercase tracking-[0.2em] text-muted-foreground leading-tight">Priority Express<br />Logistics</span>
              </div>
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 bg-primary/5 rounded-xl flex items-center justify-center text-primary">
                   <RefreshCw size={20} />
                </div>
                <span className="text-[9px] font-black uppercase tracking-[0.2em] text-muted-foreground leading-tight">30-Day Reciprocal<br />Exchange</span>
              </div>
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 bg-primary/5 rounded-xl flex items-center justify-center text-primary">
                   <ShieldCheck size={20} />
                </div>
                <span className="text-[9px] font-black uppercase tracking-[0.2em] text-muted-foreground leading-tight">Identity-Verified<br />Authentic</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Community Intelligence Section */}
      <div className="space-y-12">
        <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-10">
           <div className="space-y-3">
              <div className="flex items-center gap-3 text-primary font-black text-[10px] uppercase tracking-[0.4em]">
                 <Activity size={14} />
                 <span>Social Validation</span>
              </div>
              <h2 className="text-4xl font-black text-foreground tracking-tighter uppercase leading-none">Community Reviews</h2>
           </div>
           <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">Aggregate Rating Analytics</p>
        </header>
        
        <div className="bg-card rounded-[3.5rem] p-10 lg:p-16 border border-border shadow-2xl shadow-primary/5">
           <ReviewList productId={productId} />
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
