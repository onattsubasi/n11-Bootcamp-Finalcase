import { useProducts } from '../features/catalog/hooks/useProducts';
import { useAddToCart } from '../features/basket/hooks/useBasket';
import { ProductCard } from '../features/catalog/components/ProductCard';
import { Spinner } from '../components/ui/Spinner';
import toast from 'react-hot-toast';

const ProductListPage = () => {
  const { data, isPending, isError } = useProducts(0, 20);
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

  return (
    <div className='container mx-auto px-4 py-8'>
      <h1 className='text-3xl font-bold mb-8'>Products</h1>

      {isPending ? (
        <div className='flex justify-center items-center min-h-96'>
          <Spinner />
        </div>
      ) : isError ? (
        <div className='text-center py-10 text-red-600'>
          <p>Error loading products. Please try again later.</p>
        </div>
      ) : (
        <div className='grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6'>
          {data?.content?.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToBasket={handleAddToBasket}
              isLoading={isAdding}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default ProductListPage;
