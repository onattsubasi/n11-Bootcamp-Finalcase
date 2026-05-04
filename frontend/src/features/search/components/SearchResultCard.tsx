import React from 'react';
import { Link } from 'react-router-dom';
import { Badge } from '@/components/ui/Badge';
import { RatingStars } from '@/components/ui/RatingStars';
import { formatCurrency } from '@/lib/utils/format';
import { ProductCardModel } from '@/types/product';

interface SearchResultCardProps {
  product: ProductCardModel;
}

export const SearchResultCard: React.FC<SearchResultCardProps> = ({ product }) => {
  return (
    <Link
      to={`/product/${product.productId}`}
      className="group relative flex h-full flex-col overflow-hidden rounded-[2.5rem] border border-white/5 bg-gray-900/40 p-2 backdrop-blur-xl transition-all duration-500 hover:-translate-y-2 hover:bg-gray-900/60 hover:shadow-2xl hover:shadow-primary/20"
    >
      <div className="relative aspect-square overflow-hidden rounded-[2rem] bg-gray-800">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-700 font-black uppercase tracking-widest text-xs">No Image</div>
        )}

        {product.hasDiscount ? (
          <div className="absolute left-4 top-4">
             <Badge tone="danger" className="shadow-2xl px-4 py-1.5 font-black rounded-full text-[10px] uppercase tracking-wider">OFFER</Badge>
          </div>
        ) : null}
      </div>

      <div className="flex flex-1 flex-col gap-3 p-5">
        <div className="space-y-1">
          {product.brand ? (
            <div className="text-[10px] font-black uppercase tracking-[0.2em] text-primary/70">
              {product.brand}
            </div>
          ) : null}
          <div className="line-clamp-2 min-h-[2.5rem] text-base font-bold text-gray-100 leading-tight group-hover:text-primary transition-colors">
            {product.name}
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <RatingStars value={product.rating} size={14} />
          <span className="text-[10px] font-black text-gray-500 uppercase tracking-tighter">({product.reviewCount} reviews)</span>
        </div>

        <div className="mt-auto pt-4 flex items-end justify-between">
          {product.stockQuantity <= 0 ? (
            <Badge tone="danger" className="w-fit rounded-full px-4 font-black">OUT OF STOCK</Badge>
          ) : (
            <div className="flex flex-col">
              {product.hasDiscount && product.originalPrice ? (
                <span className="text-[10px] text-gray-500 line-through font-bold uppercase tracking-tight">
                  {formatCurrency(product.originalPrice)}
                </span>
              ) : null}
              <div className="text-2xl font-black text-white tracking-tight">
                {formatCurrency(product.price)}
              </div>
            </div>
          )}
          
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary transition-all duration-300 group-hover:bg-primary group-hover:text-white">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default SearchResultCard;
