import type { ReactNode } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

import { cn } from '@/lib/utils/cn';
import type { Category } from '@/types/product';

type CategoryBarProps = {
  categories?: Category[];
};

export const CategoryBar = ({ categories = [] }: CategoryBarProps) => {
  const [params] = useSearchParams();
  const activeCategory = params.get('category') ?? params.get('categoryId');

  return (
    <div className="flex flex-col gap-3 py-6">
      <div className="flex items-center justify-between px-1">
        <h3 className="text-[10px] font-black uppercase tracking-[0.3em] text-muted-foreground/60">
          Explore Categories
        </h3>
        <Link
          to="/search"
          className="text-[10px] font-black uppercase tracking-[0.2em] text-primary transition-opacity hover:opacity-80"
        >
          View All
        </Link>
      </div>

      <div className="no-scrollbar -mx-4 flex gap-2 overflow-x-auto px-4 pb-2 md:mx-0 md:px-0">
        <Chip to="/search" active={!activeCategory}>
          <span className="mr-2">✨</span>
          All Items
        </Chip>

        {categories.map((category) => {
          const key = category.slug ?? category.id;
          const filterValue = category.slug ?? category.id;

          return (
            <Chip
              key={key}
              to={`/search?category=${encodeURIComponent(filterValue)}`}
              active={activeCategory === filterValue}
            >
              {category.icon ? (
                <span className="mr-2">{category.icon}</span>
              ) : (
                <span className="mr-2 text-primary/40">#</span>
              )}
              {category.name}
            </Chip>
          );
        })}
      </div>
    </div>
  );
};

function Chip({ to, active, children }: { to: string; active: boolean; children: ReactNode }) {
  return (
    <Link
      to={to}
      className={cn(
        'inline-flex shrink-0 items-center whitespace-nowrap rounded-full border px-5 py-2.5 text-xs font-black uppercase tracking-widest transition-all duration-300',
        active
          ? 'z-10 scale-105 border-primary bg-primary text-primary-foreground shadow-lg shadow-primary/20'
          : 'border-border bg-card text-muted-foreground hover:border-primary/40 hover:bg-accent hover:text-foreground',
      )}
    >
      {children}
    </Link>
  );
}
