import { useCustomerCoupons } from '../hooks/usePromotion';
import { Badge } from '../../../components/ui/Badge';
import { Spinner } from '../../../components/ui/Spinner';
import { formatDate } from '../../../lib/utils/format';

const CouponsTab = () => {
  const { data, isPending, isError } = useCustomerCoupons();

  if (isPending) return <div className="p-8 flex justify-center"><Spinner /></div>;
  if (isError) return <div className="p-8 text-red-500">Failed to load coupons.</div>;

  const coupons = data || [];

  if (coupons.length === 0) {
    return <div className="p-12 text-center text-gray-500 bg-gray-50 rounded-lg border border-dashed">No active coupons available.</div>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {coupons.map((coupon) => (
        <div key={coupon.id} className="border rounded-lg p-5 bg-white shadow-sm flex flex-col justify-between hover:border-blue-300 transition-colors">
          <div>
            <div className="flex justify-between items-start mb-3">
              <span className="font-mono font-bold text-lg text-blue-600 bg-blue-50 px-2 py-1 rounded border border-blue-100">{coupon.code}</span>
              <Badge tone={coupon.status === 'ACTIVE' ? 'success' : 'neutral'}>{coupon.status}</Badge>
            </div>
            <h3 className="font-bold text-gray-900 mb-1">{coupon.promotionName || 'Promotional Discount'}</h3>
            <p className="text-sm text-gray-600 mb-4">{coupon.description || 'Use this code at checkout to get a discount.'}</p>
          </div>
          
          <div className="text-xs text-gray-500 border-t pt-3 flex justify-between">
            <span>Valid until: {coupon.expiryDate ? formatDate(coupon.expiryDate) : 'N/A'}</span>
            {coupon.minPurchaseAmount > 0 && (
              <span>Min: ${coupon.minPurchaseAmount}</span>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default CouponsTab;
