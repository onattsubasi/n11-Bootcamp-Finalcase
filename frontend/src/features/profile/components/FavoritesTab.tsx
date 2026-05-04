import React from 'react';
import { useFavorites } from '../hooks/useUserHooks';
import { ProductCard } from '../../catalog/components/ProductCard';
import { Spinner } from '@/components/ui/Spinner';
import { useAddToCart } from '@/features/basket/hooks/useBasket';
import { Heart, ShoppingBag } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

const FavoritesTab: React.FC = () => {
  const navigate = useNavigate();
  const { data, isPending, isError } = useFavorites();
  const { mutate: addToCart, isPending: isAdding } = useAddToCart();

  const handleAddToBasket = (productId: string) => {
    addToCart(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Added to secure bag.'),
        onError: () => toast.error('Failed to add item.'),
      }
    );
  };

  if (isPending) return (
    <div className="flex justify-center py-32">
      <Spinner size="lg" />
    </div>
  );

  if (isError) return (
    <div className="p-12 bg-destructive/5 border border-destructive/20 rounded-[3rem] text-destructive text-center">
       <p className="text-[10px] font-black uppercase tracking-widest">Favorites Sync Failure</p>
    </div>
  );

  const favorites = data?.content || [];

  if (favorites.length === 0) {
    return (
      <div className="py-24 text-center bg-muted/20 rounded-[3rem] border-2 border-dashed border-border group transition-all hover:border-primary/20">
         <div className="bg-muted rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
            <Heart className="text-muted-foreground/20" size={32} />
         </div>
         <h3 className="text-xl font-black text-foreground uppercase tracking-tight">Your wishlist is empty</h3>
         <p className="text-muted-foreground font-medium mt-2 mb-8 max-w-xs mx-auto text-sm">Save your favorite premium items here for quick access later.</p>
         <Button onClick={() => navigate('/')} variant="primary" className="rounded-2xl px-10 h-14 font-black uppercase tracking-widest text-[10px]">
            Explore Catalog
         </Button>
      </div>
    );
  }

  return (
    <div className="space-y-10 animate-in fade-in slide-in-from-bottom-8 duration-700">
      <header className="flex items-center justify-between border-b border-border pb-6">
         <div className="flex items-center gap-3">
            <Heart className="text-primary fill-primary" size={18} />
            <h2 className="text-[10px] font-black uppercase tracking-[0.3em] text-foreground">Curated Favorites</h2>
         </div>
         <span className="text-[10px] font-black text-muted-foreground uppercase tracking-widest">{favorites.length} Items Secured</span>
      </header>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-10">
        {favorites.map((product: any) => (
          <ProductCard
            key={product.id}
            product={product}
            onAddToBasket={handleAddToBasket}
            isLoading={isAdding}
          />
        ))}
      </div>
    </div>
  );
};

export default FavoritesTab;
