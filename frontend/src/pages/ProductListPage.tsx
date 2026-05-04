import React from 'react';
import { useProducts } from '@/features/catalog/hooks/useProducts';
import { useCategories } from '@/features/catalog/hooks/useCategories';
import { useAddToCart } from '@/features/basket/hooks/useBasket';
import { ProductCard } from '@/features/catalog/components/ProductCard';
import { HeroBanner } from '@/features/catalog/components/HeroBanner';
import { CategoryBar } from '@/features/catalog/components/CategoryBar';
import { Spinner } from '@/components/ui/Spinner';
import toast from 'react-hot-toast';

import { errorMessage } from '@/api/problem';
import { mapToProductCard } from '@/features/catalog/utils/productMapper';

const ProductListPage: React.FC = () => {
  const { data, isPending, isError, error } = useProducts(0, 24);
  const { data: categories = [] } = useCategories();
  const { mutate: addToCart, isPending: isAdding } = useAddToCart();

  const handleAddToBasket = (productId: string) => {
    addToCart(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Added to your basket!'),
        onError: (err: any) => toast.error(errorMessage(err) || 'Failed to add to basket'),
      }
    );
  };

  const products = data?.content || [];
  const hasProducts = products.length > 0;

  return (
    <div className="space-y-12 pb-20">
      {/* Hero Section */}
      <HeroBanner 
        heading="Premium Tech & Lifestyle" 
        subheading="Curated excellence for the modern explorer. Experience quality that transcends boundaries."
        count={data?.totalElements}
      />

      {/* Category Navigation */}
      <section className="container mx-auto px-4">
        <CategoryBar categories={categories} />
      </section>

      {/* Product Grid Section */}
      <section className="container mx-auto px-4 space-y-8">
        <div className="flex items-center justify-between border-b border-border pb-6">
           <h2 className="text-2xl font-black text-foreground tracking-tight uppercase">Trending Now</h2>
           {data?.totalElements ? (
             <div className="text-xs font-bold text-muted-foreground uppercase tracking-widest">
               Showing {products.length} of {data.totalElements} items
             </div>
           ) : null}
        </div>

        {isPending ? (
          /* Skeleton Grid */
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="animate-pulse space-y-4">
                <div className="aspect-square rounded-2xl bg-muted" />
                <div className="space-y-2">
                  <div className="h-3 w-1/4 rounded bg-muted" />
                  <div className="h-4 w-3/4 rounded bg-muted" />
                  <div className="h-3 w-1/2 rounded bg-muted" />
                </div>
                <div className="pt-4 flex justify-between">
                  <div className="h-6 w-1/3 rounded bg-muted" />
                  <div className="h-10 w-20 rounded-xl bg-muted" />
                </div>
              </div>
            ))}
          </div>
        ) : isError ? (
          <div className="max-w-xl mx-auto p-10 bg-destructive/5 border-2 border-dashed border-destructive/20 rounded-[2rem] text-center">
            <div className="text-4xl mb-4 text-destructive">⚠️</div>
            <h3 className="text-xl font-black text-destructive mb-2">Failed to Load Products</h3>
            <p className="text-destructive/70 font-medium mb-6">{errorMessage(error)}</p>
            <button 
              onClick={() => window.location.reload()}
              className="px-8 py-3 bg-destructive text-destructive-foreground font-black rounded-2xl hover:bg-destructive/90 transition-colors uppercase tracking-widest text-xs"
            >
              Retry Connection
            </button>
          </div>
        ) : hasProducts ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={mapToProductCard(product)}
                onAddToBasket={handleAddToBasket}
                isLoading={isAdding}
              />
            ))}
          </div>
        ) : (
          /* Empty State */
          <div className="py-24 text-center">
            <div className="text-6xl mb-6 opacity-20">🔍</div>
            <h3 className="text-2xl font-black text-foreground uppercase tracking-tight">No products found</h3>
            <p className="text-muted-foreground font-medium max-w-md mx-auto mt-2">
              We couldn't find any products matching your criteria. Try adjusting your filters or check back later!
            </p>
          </div>
        )}
      </section>
    </div>
  );
};

export default ProductListPage;
