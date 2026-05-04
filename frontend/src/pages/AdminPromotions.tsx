import { useState } from 'react';
import { 
  useAdminPromotions, 
  useActivatePromotion, 
  useUpdatePromotion, 
  useDeletePromotion,
  useAdminCouponsByPromotion,
  useCreateCouponBatch
} from '../features/admin/hooks/useAdmin';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { formatCurrency, formatDate } from '../lib/utils/format';
import toast from 'react-hot-toast';

const AdminPromotions = () => {
  const [status, setStatus] = useState('ACTIVE');
  const [selectedPromoId, setSelectedPromoId] = useState(null);
  
  const { data: promotions, isLoading: loadingPromos } = useAdminPromotions(status);
  const { data: coupons, isLoading: loadingCoupons } = useAdminCouponsByPromotion(selectedPromoId);
  
  const activateMutation = useActivatePromotion();
  const deleteMutation = useDeletePromotion();
  const batchMutation = useCreateCouponBatch();

  const handleCreateBatch = (promoId) => {
    const countStr = window.prompt('How many coupons to generate? (Max 500)');
    if (!countStr) return;
    const count = parseInt(countStr);
    if (isNaN(count) || count <= 0 || count > 500) return toast.error('Invalid count (1-500)');

    batchMutation.mutate({ promotionId: promoId, payload: { count } }, {
      onSuccess: () => toast.success(`${count} coupons generated`),
      onError: (err) => toast.error(err.message || 'Batch creation failed')
    });
  };

  return (
    <div className="p-6 space-y-8">
      <header className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Promotion & Campaigns</h1>
        <div className="flex gap-2">
           <select 
            className="border rounded px-3 py-1 text-sm bg-white"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="ACTIVE">Active</option>
            <option value="PAUSED">Paused</option>
            <option value="EXPIRED">Expired</option>
            <option value="DRAFT">Draft</option>
          </select>
          <Button size="sm" variant="primary">+ New Promotion</Button>
        </div>
      </header>

      {loadingPromos ? (
        <div className="flex justify-center p-12"><Spinner /></div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Promotions List */}
          <section className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-700">Campaigns</h2>
            {promotions?.length > 0 ? (
              promotions.map(promo => (
                <div 
                  key={promo.id} 
                  className={`p-5 rounded-xl border transition-all cursor-pointer ${selectedPromoId === promo.id ? 'border-blue-500 bg-blue-50 shadow-sm' : 'bg-white border-gray-200 hover:border-gray-300'}`}
                  onClick={() => setSelectedPromoId(promo.id)}
                >
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-bold text-gray-900">{promo.name}</h3>
                    <Badge tone={promo.status === 'ACTIVE' ? 'success' : 'neutral'}>{promo.status}</Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-4 line-clamp-2">{promo.description}</p>
                  
                  <div className="flex flex-wrap gap-2 text-xs">
                    <Badge tone="info">{promo.type}</Badge>
                    <span className="text-gray-400">Ends: {promo.endDate ? formatDate(promo.endDate) : 'Never'}</span>
                  </div>

                  {selectedPromoId === promo.id && (
                    <div className="mt-4 pt-4 border-t flex gap-2">
                      <Button size="xs" variant="outline" onClick={(e) => { e.stopPropagation(); handleCreateBatch(promo.id); }}>Generate Batch</Button>
                      <Button size="xs" variant="outline">Edit</Button>
                      <Button size="xs" variant="danger" onClick={(e) => { e.stopPropagation(); if(window.confirm('Delete?')) deleteMutation.mutate(promo.id); }}>Delete</Button>
                    </div>
                  )}
                </div>
              ))
            ) : (
              <div className="p-10 text-center text-gray-500 bg-gray-50 rounded-lg border border-dashed">No promotions found for this status.</div>
            )}
          </section>

          {/* Coupons Detail */}
          <section className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-700">Coupons for Selected</h2>
            {!selectedPromoId ? (
              <div className="p-12 text-center text-gray-400 bg-gray-50 rounded-lg border border-dashed">Select a promotion to view its coupons</div>
            ) : loadingCoupons ? (
              <div className="flex justify-center p-12"><Spinner /></div>
            ) : (
              <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50 text-[10px] text-gray-500 uppercase font-bold">
                    <tr>
                      <th className="px-4 py-2 text-left">Code</th>
                      <th className="px-4 py-2 text-left">Status</th>
                      <th className="px-4 py-2 text-left">Uses</th>
                      <th className="px-4 py-2 text-left">Expiry</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 text-sm">
                    {coupons?.map(coupon => (
                      <tr key={coupon.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 font-mono font-bold text-blue-600">{coupon.code}</td>
                        <td className="px-4 py-3"><Badge tone={coupon.status === 'ACTIVE' ? 'success' : 'neutral'} className="text-[10px]">{coupon.status}</Badge></td>
                        <td className="px-4 py-3 text-gray-600">{coupon.currentUseCount} / {coupon.maxUseCount || '∞'}</td>
                        <td className="px-4 py-3 text-xs text-gray-500">{coupon.expiryDate ? formatDate(coupon.expiryDate) : 'N/A'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!coupons?.length ? <div className="p-10 text-center text-gray-500">No coupons found. Generate a batch or add one.</div> : null}
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  );
};

export default AdminPromotions;
