import { useNavigate } from 'react-router-dom';
import { useBasketQuery, useRemoveFromBasket } from '../features/basket/hooks/useBasket';
import { Spinner } from '../components/ui/Spinner';
import { Button } from '../components/ui/Button';
import { formatCurrency } from '../lib/utils/format';
import toast from 'react-hot-toast';

const BasketPage = () => {
  const navigate = useNavigate();
  const { data: basket, isPending } = useBasketQuery(true);
  const { mutate: removeItem, isPending: isRemoving } = useRemoveFromBasket();

  const handleRemoveItem = (itemId) => {
    removeItem(itemId, {
      onSuccess: () => toast.success('Item removed'),
      onError: () => toast.error('Failed to remove item'),
    });
  };

  if (isPending) {
    return (
      <div className='flex justify-center items-center min-h-96'>
        <Spinner />
      </div>
    );
  }

  const isEmpty = !basket?.items || basket.items.length === 0;

  if (isEmpty) {
    return (
      <div className='text-center py-20 bg-white rounded-lg shadow-sm border border-gray-100'>
        <div className='text-6xl mb-4'>🛒</div>
        <h2 className='text-3xl font-bold text-gray-800'>Your basket is empty</h2>
        <p className='text-gray-500 mt-2 mb-8'>Looks like you haven\'t added anything yet.</p>
        <Button onClick={() => navigate('/')} variant='primary'>
          Start Shopping
        </Button>
      </div>
    );
  }

  return (
    <div className='max-w-4xl mx-auto space-y-8'>
      <h2 className='text-4xl font-bold text-gray-900'>Your Shopping Bag</h2>
      <div className='grid grid-cols-1 lg:grid-cols-3 gap-8'>
        <div className='lg:col-span-2 space-y-4'>
          {basket.items.map((item) => (
            <div
              key={item.id}
              className='bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex items-center space-x-6 hover:border-blue-200 transition-colors'
            >
              <div className='w-24 h-24 bg-gray-100 rounded flex-shrink-0 overflow-hidden'>
                <img
                  src={item.image || 'https://via.placeholder.com/100'}
                  alt={item.name}
                  className='w-full h-full object-cover'
                />
              </div>
              <div className='flex-grow'>
                <h3 className='text-lg font-bold text-gray-900'>{item.name}</h3>
                <p className='text-gray-500'>Quantity: {item.quantity}</p>
              </div>
              <div className='text-right'>
                <p className='text-xl font-bold text-blue-600'>{formatCurrency(item.price * item.quantity, basket.currency)}</p>
                <button
                  onClick={() => handleRemoveItem(item.id)}
                  disabled={isRemoving}
                  className='text-red-500 hover:text-red-700 text-sm font-semibold mt-2 disabled:opacity-50'
                >
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
        <div className='bg-gray-900 text-white p-8 rounded-lg h-fit sticky top-4 shadow-lg'>
          <h3 className='text-2xl font-bold mb-6'>Summary</h3>
          <div className='space-y-4 mb-8'>
            <div className='flex justify-between text-gray-400'>
              <span>Subtotal</span>
              <span>{formatCurrency(basket.totalPrice || 0, basket.currency)}</span>
            </div>
            <div className='flex justify-between text-gray-400'>
              <span>Shipping</span>
              <span className='text-green-400 font-bold'>Free</span>
            </div>
            <div className='border-t border-gray-800 pt-4 flex justify-between text-xl font-bold'>
              <span>Total</span>
              <span className='text-blue-400'>{formatCurrency(basket.totalPrice || 0, basket.currency)}</span>
            </div>
          </div>
          <Button
            onClick={() => navigate('/checkout')}
            className='w-full'
            variant='primary'
          >
            Checkout Now
          </Button>
        </div>
      </div>
    </div>
  );
};

export default BasketPage;
