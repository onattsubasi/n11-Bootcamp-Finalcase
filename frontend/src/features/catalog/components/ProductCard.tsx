import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Package, Heart, Sparkles, AlertTriangle } from 'lucide-react';
import { RatingStars } from '@/components/ui/RatingStars';
import { formatTRY } from '@/lib/utils/format';
import { ProductCardModel } from '@/types/product';
import { cn } from '@/lib/utils/cn';

interface ProductCardProps {
  product: ProductCardModel;
  onAddToBasket?: (productId: string) => void;
  isLoading?: boolean;
}

export const ProductCard: React.FC<ProductCardProps> = ({ 
  product, 
  onAddToBasket, 
  isLoading = false 
}) => {
  const isOutOfStock = (product.stockQuantity ?? 0) <= 0;
  const hasDiscount = product.hasDiscount;

  return (
    <div className="group relative flex flex-col overflow-hidden rounded-[2.5rem] border border-border bg-card shadow-sm transition-all duration-700 hover:-translate-y-2 hover:shadow-3xl hover:border-primary/20">
      {/* Interaction Layer */}
      <div className="absolute top-4 right-4 z-20 flex flex-col gap-2">
         <button className="p-2.5 rounded-xl bg-white/80 backdrop-blur-md border border-border text-muted-foreground hover:text-destructive hover:scale-110 transition-all opacity-0 group-hover:opacity-100 duration-500 shadow-xl shadow-primary/5">
            <Heart size={16} />
         </button>
      </div>

      {/* Media Engine */}
      <Link to={`/product/${product.productId}`} className="relative block overflow-hidden aspect-[4/5] bg-muted/30">
        <div className="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-700" />
        
        {product.imageUrl ? (
          <img 
            src={product.imageUrl} 
            alt={product.name} 
            loading="lazy"
            className="h-full w-full object-contain p-8 mix-blend-multiply transition-transform duration-1000 group-hover:scale-110" 
          />
        ) : (
          <div className="flex h-full w-full flex-col items-center justify-center gap-4 text-muted-foreground/20">
            <Package size={60} strokeWidth={1} />
            <span className="text-[10px] font-black uppercase tracking-[0.4em]">SKU VOID</span>
          </div>
        )}
        
        {/* Promotion Badges */}
        <div className="absolute top-6 left-6 flex flex-col gap-2">
          {hasDiscount && (
            <div className="bg-destructive text-destructive-foreground px-3 py-1.5 rounded-xl font-black text-[10px] uppercase tracking-widest shadow-xl shadow-destructive/20 animate-pulse-soft">
              -{Math.round(((product.originalPrice! - product.price) / product.originalPrice!) * 100)}%
            </div>
          )}
          {product.promotionBadge && (
            <div className="bg-gray-950 text-white px-3 py-1.5 rounded-xl font-black text-[10px] uppercase tracking-widest shadow-xl flex items-center gap-2 border border-white/10">
               <Sparkles size={10} className="text-primary" />
               {product.promotionBadge}
            </div>
          )}
        </div>

        {/* Stock Status Indicator */}
        <div className="absolute bottom-6 left-6">
          {isOutOfStock ? (
            <div className="bg-destructive/10 backdrop-blur-md text-destructive px-3 py-1.5 rounded-xl font-black text-[10px] uppercase tracking-widest border border-destructive/20 flex items-center gap-2">
               <AlertTriangle size={10} /> SOLD OUT
            </div>
          ) : product.stockQuantity < 10 ? (
            <div className="bg-amber-500/10 backdrop-blur-md text-amber-600 px-3 py-1.5 rounded-xl font-black text-[10px] uppercase tracking-widest border border-amber-500/20">
               CRITICAL STOCK: {product.stockQuantity}
            </div>
          ) : null}
        </div>

        {/* Tactical Injection (Add to Bag) */}
        {!isOutOfStock && (
          <button
            type="button"
            disabled={isLoading}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onAddToBasket?.(product.productId);
            }}
            className="absolute bottom-6 right-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-2xl shadow-primary/30 translate-y-12 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-700 hover:scale-110 active:scale-95"
          >
            <ShoppingCart size={22} />
          </button>
        )}
      </Link>

      {/* Intelligence Area */}
      <div className="flex flex-1 flex-col gap-4 p-8">
        <div className="space-y-1">
           {product.brand && (
             <div className="text-[10px] font-black uppercase tracking-[0.3em] text-primary/60">
               {product.brand}
             </div>
           )}
           <Link to={`/product/${product.productId}`} className="block">
             <h3 className="line-clamp-1 text-lg font-black text-foreground uppercase tracking-tight group-hover:text-primary transition-colors duration-300">
               {product.name}
             </h3>
           </Link>
        </div>

        <div className="flex items-center gap-3">
          <RatingStars value={product.rating} size={14} />
          <span className="text-[10px] font-black text-muted-foreground uppercase tracking-widest pt-0.5">[{product.reviewCount}]</span>
        </div>

        <div className="mt-auto pt-4 flex items-end justify-between border-t border-border/50">
           <div className="flex flex-col">
              {hasDiscount && product.originalPrice && (
                <span className="text-xs text-muted-foreground line-through font-bold decoration-destructive/30">
                  {formatTRY(product.originalPrice)}
                </span>
              )}
              <span className="text-2xl font-black text-foreground tracking-tighter tabular-nums leading-none">
                {formatTRY(product.price)}
              </span>
           </div>
           <div className="text-[8px] font-black text-muted-foreground uppercase tracking-[0.2em]">Verified SKU</div>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
