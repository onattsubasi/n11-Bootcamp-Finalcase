import { useSearchParams } from 'react-router-dom';
import { X } from 'lucide-react';
import { Card } from '../../../components/ui/Card';
import { RatingStars } from '../../../components/ui/RatingStars';
import { formatCurrency } from '../../../lib/utils/format';

const setParam = (params, key, value) => {
  const next = new URLSearchParams(params);

  if (value === undefined || value === '') {
    next.delete(key);
  } else {
    next.set(key, value);
  }

  next.delete('page');
  return next;
};

export const FacetSidebar = ({ facets }) => {
  const [params, setParams] = useSearchParams();
  const activeCategory = params.get('categoryId') ?? '';
  const activeBrand = params.get('brandIds') ?? '';
  const activeMinRating = params.get('minRating') ?? '';
  const minPrice = params.get('minPrice') ?? '';
  const maxPrice = params.get('maxPrice') ?? '';

  const hasAnyFilter = activeCategory || activeBrand || activeMinRating || minPrice || maxPrice;

  const clearAll = () => {
    const next = new URLSearchParams(params);
    ['categoryId', 'brandIds', 'minRating', 'minPrice', 'maxPrice', 'page'].forEach((key) => next.delete(key));
    setParams(next);
  };

  const brandEntries = Object.entries(facets?.brands ?? {});
  const categoryEntries = Object.entries(facets?.categories ?? {});

  return (
    <aside className="space-y-4">
      {hasAnyFilter ? (
        <button
          type="button"
          onClick={clearAll}
          className="flex items-center gap-1.5 text-sm font-medium text-blue-600 hover:underline"
        >
          <X className="h-3.5 w-3.5" /> Clear all filters
        </button>
      ) : null}

      {categoryEntries.length > 0 ? (
        <Card className="p-4">
          <h3 className="mb-3 text-sm font-semibold">Category</h3>
          <div className="space-y-1.5">
            {categoryEntries.map(([name, count]) => (
              <label key={name} className="flex cursor-pointer items-center gap-2 text-sm">
                <input
                  type="radio"
                  name="category-facet"
                  checked={activeCategory === name}
                  onChange={() => setParams(setParam(params, 'categoryId', activeCategory === name ? undefined : name))}
                  className="accent-blue-600"
                />
                <span className="flex-1">{name}</span>
                <span className="text-xs text-gray-500">{count}</span>
              </label>
            ))}
          </div>
        </Card>
      ) : null}

      {brandEntries.length > 0 ? (
        <Card className="p-4">
          <h3 className="mb-3 text-sm font-semibold">Brand</h3>
          <div className="max-h-60 space-y-1.5 overflow-y-auto">
            {brandEntries.map(([name, count]) => (
              <label key={name} className="flex cursor-pointer items-center gap-2 text-sm">
                <input
                  type="radio"
                  name="brand-facet"
                  checked={activeBrand === name}
                  onChange={() => setParams(setParam(params, 'brandIds', activeBrand === name ? undefined : name))}
                  className="accent-blue-600"
                />
                <span className="flex-1">{name}</span>
                <span className="text-xs text-gray-500">{count}</span>
              </label>
            ))}
          </div>
        </Card>
      ) : null}

      <Card className="p-4">
        <h3 className="mb-3 text-sm font-semibold">Price</h3>
        {facets ? (
          <div className="mb-2 text-xs text-gray-500">
            {formatCurrency(facets.price?.min ?? 0)} &ndash; {formatCurrency(facets.price?.max ?? 0)}
          </div>
        ) : null}
        <div className="flex gap-2">
          <input
            type="number"
            min={0}
            placeholder="Min"
            value={minPrice}
            onChange={(event) => setParams(setParam(params, 'minPrice', event.target.value || undefined))}
            className="h-9 w-full rounded-lg border border-gray-300 bg-white px-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="number"
            min={0}
            placeholder="Max"
            value={maxPrice}
            onChange={(event) => setParams(setParam(params, 'maxPrice', event.target.value || undefined))}
            className="h-9 w-full rounded-lg border border-gray-300 bg-white px-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </Card>

      <Card className="p-4">
        <h3 className="mb-3 text-sm font-semibold">Rating</h3>
        <div className="space-y-1.5">
          {[4, 3, 2, 1].map((min) => (
            <label key={min} className="flex cursor-pointer items-center gap-2 text-sm">
              <input
                type="radio"
                name="rating-facet"
                checked={activeMinRating === String(min)}
                onChange={() => setParams(setParam(params, 'minRating', activeMinRating === String(min) ? undefined : String(min)))}
                className="accent-blue-600"
              />
              <RatingStars value={min} size={12} />
              <span className="text-xs text-gray-500">and up</span>
            </label>
          ))}
        </div>
      </Card>
    </aside>
  );
};

export default FacetSidebar;