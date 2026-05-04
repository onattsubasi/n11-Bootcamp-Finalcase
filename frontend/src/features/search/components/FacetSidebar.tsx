import React from 'react';
import { useSearchParams } from 'react-router-dom';
import { X } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { RatingStars } from '@/components/ui/RatingStars';
import { formatCurrency } from '@/lib/utils/format';

interface Facets {
  brands?: Record<string, number>;
  categories?: Record<string, number>;
  price?: {
    min: number;
    max: number;
  };
}

interface FacetSidebarProps {
  facets?: Facets;
}

const setParam = (params: URLSearchParams, key: string, value: string | undefined) => {
  const next = new URLSearchParams(params);

  if (value === undefined || value === '') {
    next.delete(key);
  } else {
    next.set(key, value);
  }

  next.delete('page');
  return next;
};

export const FacetSidebar: React.FC<FacetSidebarProps> = ({ facets }) => {
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
    <aside className="space-y-6">
      {hasAnyFilter ? (
        <button
          type="button"
          onClick={clearAll}
          className="flex w-full items-center justify-center gap-2 rounded-2xl bg-primary/10 py-3 text-sm font-bold text-primary hover:bg-primary/20 transition-all"
        >
          <X className="h-4 w-4" /> Clear all filters
        </button>
      ) : null}

      {categoryEntries.length > 0 ? (
        <Card className="overflow-hidden border-white/5 bg-gray-900/50 backdrop-blur-xl">
          <div className="bg-white/5 px-4 py-3 border-b border-white/5">
            <h3 className="text-sm font-bold uppercase tracking-wider text-gray-400">Category</h3>
          </div>
          <div className="p-4 space-y-2">
            {categoryEntries.map(([name, count]) => (
              <label key={name} className="group flex cursor-pointer items-center gap-3 text-sm transition-colors hover:text-primary">
                <div className="relative flex h-5 w-5 items-center justify-center">
                  <input
                    type="radio"
                    name="category-facet"
                    checked={activeCategory === name}
                    onChange={() => setParams(setParam(params, 'categoryId', activeCategory === name ? undefined : name))}
                    className="peer absolute h-full w-full cursor-pointer opacity-0"
                  />
                  <div className="h-4 w-4 rounded-full border-2 border-white/20 transition-all peer-checked:border-primary peer-checked:bg-primary" />
                </div>
                <span className="flex-1 font-medium">{name}</span>
                <span className="rounded-full bg-white/5 px-2 py-0.5 text-[10px] font-bold text-gray-500 group-hover:bg-primary/20 group-hover:text-primary">
                  {count}
                </span>
              </label>
            ))}
          </div>
        </Card>
      ) : null}

      {brandEntries.length > 0 ? (
        <Card className="overflow-hidden border-white/5 bg-gray-900/50 backdrop-blur-xl">
          <div className="bg-white/5 px-4 py-3 border-b border-white/5">
            <h3 className="text-sm font-bold uppercase tracking-wider text-gray-400">Brand</h3>
          </div>
          <div className="max-h-64 overflow-y-auto p-4 space-y-2 scrollbar-hide">
            {brandEntries.map(([name, count]) => (
              <label key={name} className="group flex cursor-pointer items-center gap-3 text-sm transition-colors hover:text-primary">
                <div className="relative flex h-5 w-5 items-center justify-center">
                  <input
                    type="radio"
                    name="brand-facet"
                    checked={activeBrand === name}
                    onChange={() => setParams(setParam(params, 'brandIds', activeBrand === name ? undefined : name))}
                    className="peer absolute h-full w-full cursor-pointer opacity-0"
                  />
                  <div className="h-4 w-4 rounded-full border-2 border-white/20 transition-all peer-checked:border-primary peer-checked:bg-primary" />
                </div>
                <span className="flex-1 font-medium">{name}</span>
                <span className="rounded-full bg-white/5 px-2 py-0.5 text-[10px] font-bold text-gray-500 group-hover:bg-primary/20 group-hover:text-primary">
                  {count}
                </span>
              </label>
            ))}
          </div>
        </Card>
      ) : null}

      <Card className="overflow-hidden border-white/5 bg-gray-900/50 backdrop-blur-xl">
        <div className="bg-white/5 px-4 py-3 border-b border-white/5">
          <h3 className="text-sm font-bold uppercase tracking-wider text-gray-400">Price Range</h3>
        </div>
        <div className="p-4 space-y-4">
          {facets?.price ? (
            <div className="flex items-center justify-between text-[10px] font-bold text-gray-500 uppercase">
              <span>{formatCurrency(facets.price.min)}</span>
              <div className="h-px flex-1 mx-2 bg-white/5" />
              <span>{formatCurrency(facets.price.max)}</span>
            </div>
          ) : null}
          <div className="flex gap-2">
            <input
              type="number"
              min={0}
              placeholder="Min"
              value={minPrice}
              onChange={(event) => setParams(setParam(params, 'minPrice', event.target.value || undefined))}
              className="h-10 w-full rounded-xl border border-white/5 bg-white/5 px-3 text-sm font-medium focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
            />
            <input
              type="number"
              min={0}
              placeholder="Max"
              value={maxPrice}
              onChange={(event) => setParams(setParam(params, 'maxPrice', event.target.value || undefined))}
              className="h-10 w-full rounded-xl border border-white/5 bg-white/5 px-3 text-sm font-medium focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
            />
          </div>
        </div>
      </Card>

      <Card className="overflow-hidden border-white/5 bg-gray-900/50 backdrop-blur-xl">
        <div className="bg-white/5 px-4 py-3 border-b border-white/5">
          <h3 className="text-sm font-bold uppercase tracking-wider text-gray-400">Minimum Rating</h3>
        </div>
        <div className="p-4 space-y-2">
          {[4, 3, 2, 1].map((min) => (
            <label key={min} className="group flex cursor-pointer items-center gap-3 text-sm transition-colors hover:text-primary">
              <div className="relative flex h-5 w-5 items-center justify-center">
                <input
                  type="radio"
                  name="rating-facet"
                  checked={activeMinRating === String(min)}
                  onChange={() => setParams(setParam(params, 'minRating', activeMinRating === String(min) ? undefined : String(min)))}
                  className="peer absolute h-full w-full cursor-pointer opacity-0"
                />
                <div className="h-4 w-4 rounded-full border-2 border-white/20 transition-all peer-checked:border-primary peer-checked:bg-primary" />
              </div>
              <div className="flex flex-1 items-center gap-2">
                <RatingStars value={min} size={14} />
                <span className="text-[10px] font-bold text-gray-500 uppercase group-hover:text-primary/70">and up</span>
              </div>
            </label>
          ))}
        </div>
      </Card>
    </aside>
  );
};

export default FacetSidebar;