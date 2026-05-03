import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { Badge } from '../../../components/ui/Badge';
import { RatingStars } from '../../../components/ui/RatingStars';
import { formatCurrency } from '../../../lib/utils/format';

export const ProductCard = ({ product, onAddToBasket, isLoading = false }) => {
  const stockCount = product.stockQuantity ?? product.stock ?? 0;

  return (
    <div className="flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      <Link to={`/product/${product.id}`} className="block">
        <div className="flex h-48 items-center justify-center bg-gray-100 text-gray-500">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            '[Image]'
          )}
        </div>
      </Link>

      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="flex items-start justify-between gap-3">
          <Link to={`/product/${product.id}`} className="min-w-0 flex-1">
            <h3 className="line-clamp-2 text-lg font-semibold text-gray-900">{product.name}</h3>
          </Link>
          {stockCount > 0 ? <Badge tone="success">In Stock</Badge> : <Badge tone="danger">Sold Out</Badge>}
        </div>

        <p className="line-clamp-2 flex-1 text-sm text-gray-600">{product.description || 'No description available.'}</p>

        <div className="flex items-center gap-2">
          <RatingStars value={product.rating ?? 0} size={14} />
          <span className="text-sm text-gray-500">{product.rating ? product.rating.toFixed(1) : 'No rating'}</span>
        </div>

        <div className="mt-auto flex items-center justify-between gap-3 pt-2">
          <span className="text-xl font-bold text-gray-900">{formatCurrency(product.price ?? 0)}</span>
          <Button
            type="button"
            variant="primary"
            size="sm"
            disabled={isLoading || stockCount <= 0}
            onClick={() => onAddToBasket?.(product.id)}
          >
            {stockCount > 0 ? 'Add to Basket' : 'Unavailable'}
          </Button>
        </div>
      </div>
    </div>
  );
};