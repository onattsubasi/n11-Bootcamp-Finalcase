import { Link } from 'react-router-dom';
import { Badge } from '../../../components/ui/Badge';
import { RatingStars } from '../../../components/ui/RatingStars';
import { formatCurrency } from '../../../lib/utils/format';

export const SearchResultCard = ({ product }) => {
  const hasDiscount = (product.discountPercentage ?? 0) > 0;

  return (
    <Link
      to={`/product/${product.id}`}
      className="group flex h-full flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg"
    >
      <div className="relative aspect-square overflow-hidden bg-gray-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">[Image]</div>
        )}

        {hasDiscount ? (
          <span className="absolute left-2 top-2 rounded-full bg-red-500 px-2.5 py-1 text-[11px] font-bold text-white shadow">
            %{product.discountPercentage}
          </span>
        ) : null}
      </div>

      <div className="flex flex-1 flex-col gap-1 p-3">
        {product.brand ? <div className="text-[11px] font-semibold uppercase tracking-wider text-gray-500">{product.brand}</div> : null}
        <div className="line-clamp-2 min-h-[2.5rem] text-sm font-medium text-gray-900">{product.name}</div>
        <div className="mt-1 flex items-center gap-1 text-xs text-gray-500">
          <RatingStars value={product.rating ?? 0} size={12} />
          <span>({product.reviewCount ?? 0})</span>
        </div>

        <div className="mt-auto pt-2">
          {(product.stockQuantity ?? 0) <= 0 ? (
            <Badge tone="danger">Out of stock</Badge>
          ) : hasDiscount ? (
            <>
              <div className="text-xs text-gray-500 line-through">{formatCurrency(product.price ?? 0)}</div>
              <div className="text-lg font-bold text-emerald-600">{formatCurrency(product.discountedPrice ?? product.price ?? 0)}</div>
            </>
          ) : (
            <div className="text-lg font-bold text-emerald-600">{formatCurrency(product.price ?? 0)}</div>
          )}
        </div>
      </div>
    </Link>
  );
};

export default SearchResultCard;