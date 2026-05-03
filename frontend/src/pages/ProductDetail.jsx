import { ShoppingCart, Heart, ListPlus } from 'lucide-react';
import { useProduct } from '../features/catalog/hooks/useProduct';
import { useAddToCart } from '../features/basket/hooks/useBasket';
import { 
  useFavorites, 
  useAddFavorite, 
  useRemoveFavorite,
  useProductLists,
  useAddProductListItem
} from '../features/profile/hooks/useUserHooks';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { RatingStars } from '../components/ui/RatingStars';
import { Spinner } from '../components/ui/Spinner';
import { formatCurrency } from '../lib/utils/format';
import { ReviewList } from '../features/reviews/components/ReviewList';
import { useStore } from '../store';
import toast from 'react-hot-toast';
import { useState } from 'react';

const ProductDetail = () => {
  const { id } = useParams();
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const { data: product, isLoading, isError } = useProduct(id);
  const { mutate: addToCart, isPending: isAdding } = useAddToCart();
  
  // Favorites
  const { data: favorites } = useFavorites(0, 100);
  const addFavoriteMutation = useAddFavorite();
  const removeFavoriteMutation = useRemoveFavorite();
  const isFavorite = favorites?.items?.some(f => f.productId === id);

  // Lists
  const { data: lists } = useProductLists();
  const addToListMutation = useAddProductListItem();
  const [showListSelector, setShowListSelector] = useState(false);

  if (isLoading) {
    return (
      <div className="flex min-h-96 items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (isError) return <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-600">Error loading product.</div>;
  if (!product) return <div className="py-16 text-center text-gray-500">Product not found.</div>;

  const handleAddToBasket = () => {
    if (!isAuthenticated) {
      toast.error('Please sign in to add items to the basket.');
      return;
    }

    addToCart(
      { productId: product.id, quantity: 1 },
      {
        onSuccess: () => toast.success('Added to basket!'),
        onError: () => toast.error('Failed to add to basket'),
      }
    );
  };

  const toggleFavorite = () => {
    if (!isAuthenticated) return toast.error('Sign in first');
    
    if (isFavorite) {
      removeFavoriteMutation.mutate(product.id, { onSuccess: () => toast.success('Removed from favorites') });
    } else {
      addFavoriteMutation.mutate(product.id, { onSuccess: () => toast.success('Added to favorites') });
    }
  };

  const addToList = (listId) => {
    addToListMutation.mutate({ listId, productId: product.id }, {
      onSuccess: () => {
        toast.success('Added to list');
        setShowListSelector(false);
      }
    });
  };

  return (
    <div className="mx-auto max-w-5xl space-y-8">
      <section className="overflow-hidden rounded-xl bg-white shadow-sm">
        <div className="grid gap-0 md:grid-cols-2">
          <div className="flex min-h-80 items-center justify-center bg-gray-100">
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
            ) : (
              <span className="text-gray-500">[Product Image]</span>
            )}
          </div>

          <div className="flex flex-col gap-4 p-6 sm:p-8">
            <div className="flex flex-wrap items-center gap-2">
              {product.brand ? <Badge tone="info">{product.brand}</Badge> : null}
              {product.stockQuantity > 0 ? <Badge tone="success">In Stock</Badge> : <Badge tone="danger">Out of stock</Badge>}
            </div>

            <h1 className="text-3xl font-bold tracking-tight text-gray-900">{product.name}</h1>

            <div className="flex flex-wrap items-center gap-2 text-sm text-gray-600">
              <RatingStars value={product.rating ?? 0} />
              <span className="font-medium text-gray-900">{(product.rating ?? 0).toFixed(1)}</span>
              <span>({product.reviewCount ?? 0} reviews)</span>
            </div>

            <p className="text-sm leading-relaxed text-gray-600">{product.description}</p>

            <div className="rounded-xl bg-gray-50 p-4">
              <div className="text-3xl font-extrabold text-emerald-600">{formatCurrency(product.discountedPrice ?? product.price ?? 0, product.currency)}</div>
              {product.discountPercentage > 0 ? (
                <Badge tone="warning" className="mt-2">
                  {product.discountPercentage}% off
                </Badge>
              ) : null}
            </div>

            <div className="flex gap-2">
              <Button
                type="button"
                variant="primary"
                size="lg"
                className="flex-1"
                disabled={isAdding || product.stockQuantity === 0}
                onClick={handleAddToBasket}
              >
                <ShoppingCart className="h-5 w-5" />
                {product.stockQuantity === 0 ? 'Out of stock' : 'Add to basket'}
              </Button>
              
              <Button
                type="button"
                variant="outline"
                size="lg"
                onClick={toggleFavorite}
                className={isFavorite ? 'text-red-500 border-red-200 bg-red-50' : ''}
              >
                <Heart className={`h-5 w-5 ${isFavorite ? 'fill-current' : ''}`} />
              </Button>

              <div className="relative">
                <Button
                  type="button"
                  variant="outline"
                  size="lg"
                  onClick={() => setShowListSelector(!showListSelector)}
                >
                  <ListPlus className="h-5 w-5" />
                </Button>
                
                {showListSelector && (
                  <div className="absolute right-0 bottom-full mb-2 w-48 bg-white border rounded-xl shadow-xl z-10 overflow-hidden">
                    <div className="p-2 border-b text-xs font-bold text-gray-400 uppercase tracking-wider">Save to List</div>
                    {lists?.map(list => (
                      <button 
                        key={list.id}
                        onClick={() => addToList(list.id)}
                        className="w-full text-left px-4 py-2 text-sm hover:bg-gray-50 transition-colors"
                      >
                        {list.name}
                      </button>
                    ))}
                    {!lists?.length && <div className="p-4 text-xs text-gray-400">No lists found</div>}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>

      <ReviewList productId={product.id} />
    </div>
  );
};

export default ProductDetail;
