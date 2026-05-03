import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCheckoutQuote, useSubmitCheckout } from '../features/checkout/hooks/useCheckout';
import { useAddresses } from '../features/profile/hooks/useAddresses';
import { useBasketQuery } from '../features/basket/hooks/useBasket';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { formatCurrency } from '../lib/utils/format';
import toast from 'react-hot-toast';

const CheckoutPage = () => {
  const navigate = useNavigate();
  const { data: basket } = useBasketQuery(true);
  const { data: addresses } = useAddresses();
  const { mutate: getQuote, data: quote, isPending: isQuoting } = useCheckoutQuote();
  const { mutate: submitCheckout, isPending: isSubmitting } = useSubmitCheckout();
  
  const [selectedShippingId, setSelectedShippingId] = useState('');
  const [selectedBillingId, setSelectedBillingId] = useState('');
  const [couponCode, setCouponCode] = useState('');

  // Auto-select defaults
  useEffect(() => {
    if (addresses?.length > 0) {
      const defShipping = addresses.find(a => a.isDefaultShipping) || addresses[0];
      const defBilling = addresses.find(a => a.isDefaultBilling) || addresses[0];
      setSelectedShippingId(defShipping.id);
      setSelectedBillingId(defBilling.id);
    }
  }, [addresses]);

  // Fetch quote when addresses or coupon change
  useEffect(() => {
    if (selectedShippingId && selectedBillingId) {
      getQuote({
        shippingAddressId: selectedShippingId,
        billingAddressId: selectedBillingId,
        couponCode: couponCode || undefined
      });
    }
  }, [selectedShippingId, selectedBillingId, couponCode, getQuote]);

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!selectedShippingId || !selectedBillingId) {
      toast.error('Please select shipping and billing addresses');
      return;
    }

    submitCheckout(
      {
        shippingAddressId: selectedShippingId,
        billingAddressId: selectedBillingId,
        couponCode: couponCode || undefined,
        paymentMethod: {
          provider: 'iyzico',
          methodType: 'CREDIT_CARD',
          paymentToken: null,
          useThreeDSecure: false
        }
      },
      {
        onSuccess: (response) => {
          if (!response.redirectUrl) {
            toast.success('Order placed successfully!');
            setTimeout(() => navigate('/orders'), 1500);
          }
        },
        onError: (error) => {
          toast.error(error.message || 'Checkout failed. Please try again.');
        },
      }
    );
  };

  const isPending = isQuoting || isSubmitting;

  return (
    <div className='max-w-4xl mx-auto py-12 px-4'>
      <div className='grid grid-cols-1 md:grid-cols-2 gap-8'>
        <div className='space-y-8'>
          <section className='bg-white p-6 rounded-lg shadow'>
            <h2 className='text-xl font-bold mb-4'>1. Shipping Address</h2>
            <div className='space-y-3'>
              {addresses?.map((addr) => (
                <label key={addr.id} className={`block p-4 border rounded cursor-pointer transition-colors ${selectedShippingId === addr.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200'}`}>
                  <input 
                    type='radio' 
                    name='shipping' 
                    className='hidden' 
                    value={addr.id} 
                    checked={selectedShippingId === addr.id}
                    onChange={(e) => setSelectedShippingId(e.target.value)}
                  />
                  <p className='font-semibold'>{addr.title}</p>
                  <p className='text-sm text-gray-600'>{addr.addressLine1}, {addr.city}</p>
                </label>
              ))}
              {!addresses?.length && <p className='text-gray-500 italic'>No addresses found. Add one in profile.</p>}
            </div>
          </section>

          <section className='bg-white p-6 rounded-lg shadow'>
            <h2 className='text-xl font-bold mb-4'>2. Billing Address</h2>
            <div className='space-y-3'>
              {addresses?.map((addr) => (
                <label key={addr.id} className={`block p-4 border rounded cursor-pointer transition-colors ${selectedBillingId === addr.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200'}`}>
                  <input 
                    type='radio' 
                    name='billing' 
                    className='hidden' 
                    value={addr.id} 
                    checked={selectedBillingId === addr.id}
                    onChange={(e) => setSelectedBillingId(e.target.value)}
                  />
                  <p className='font-semibold'>{addr.title}</p>
                  <p className='text-sm text-gray-600'>{addr.addressLine1}, {addr.city}</p>
                </label>
              ))}
            </div>
          </section>

          <section className='bg-white p-6 rounded-lg shadow'>
            <h2 className='text-xl font-bold mb-4'>3. Coupon Code</h2>
            <Input 
              placeholder='Enter coupon code' 
              value={couponCode}
              onChange={(e) => setCouponCode(e.target.value)}
            />
          </section>
        </div>

        <div className='space-y-8'>
          <section className='bg-white p-6 rounded-lg shadow sticky top-4'>
            <h2 className='text-xl font-bold mb-4'>Order Summary</h2>
            <div className='space-y-4'>
              {basket?.items?.map((item) => (
                <div key={item.id} className='flex justify-between text-sm'>
                  <span>{item.name} (x{item.quantity})</span>
                  <span>{formatCurrency(item.price * item.quantity, basket.currency)}</span>
                </div>
              ))}
              
              <div className='border-t pt-4 space-y-2'>
                <div className='flex justify-between text-gray-600'>
                  <span>Subtotal</span>
                  <span>{formatCurrency(quote?.money?.subtotalAmount || 0, quote?.money?.currency)}</span>
                </div>
                {quote?.money?.discountAmount > 0 && (
                  <div className='flex justify-between text-green-600'>
                    <span>Discount</span>
                    <span>-{formatCurrency(quote.money.discountAmount, quote.money.currency)}</span>
                  </div>
                )}
                <div className='flex justify-between text-gray-600'>
                  <span>Shipping</span>
                  <span>{formatCurrency(quote?.money?.shippingAmount || 0, quote?.money?.currency)}</span>
                </div>
                <div className='flex justify-between text-gray-600'>
                  <span>Tax</span>
                  <span>{formatCurrency(quote?.money?.taxAmount || 0, quote?.money?.currency)}</span>
                </div>
                <div className='flex justify-between font-bold text-lg border-t pt-2'>
                  <span>Total</span>
                  <span>{formatCurrency(quote?.money?.grandTotalAmount || 0, quote?.money?.currency)}</span>
                </div>
              </div>

              <Button
                onClick={handleSubmit}
                variant='primary'
                disabled={isPending || !quote}
                className='w-full py-4 text-lg mt-4'
              >
                {isSubmitting ? 'Processing...' : 'Pay & Complete Order'}
              </Button>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage;
