import { useFavorites } from '../hooks/useUserHooks';
import { ProductCard } from '../../catalog/components/ProductCard';
import { Spinner } from '../../../components/ui/Spinner';
import { useAddToCart } from '../../basket/hooks/useBasket';
import toast from 'react-hot-toast';

const FavoritesTab = () => {
  const { data, isPending, isError } = useFavorites();
  const { mutate: addToCart, isPending: isAdding } = useAddToCart();

  const handleAddToBasket = (productId) => {
    addToCart(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Added to basket!'),
        onError: () => toast.error('Failed to add to basket'),
      }
    );
  };

  if (isPending) return <div className="p-8 flex justify-center"><Spinner /></div>;
  if (isError) return <div className="p-8 text-red-500">Failed to load favorites.</div>;

  const favorites = data?.content || [];

  if (favorites.length === 0) {
    return <div className="p-12 text-center text-gray-500 bg-gray-50 rounded-lg border border-dashed">You have no favorites yet.</div>;
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {favorites.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          onAddToBasket={handleAddToBasket}
          isLoading={isAdding}
        />
      ))}
    </div>
  );
};

export default FavoritesTab;
