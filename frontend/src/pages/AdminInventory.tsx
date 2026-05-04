import { useInventoryItems, useSetStock } from '../features/inventory/hooks/useInventory';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { useState } from 'react';

const AdminInventory = () => {
  const { data, isLoading } = useInventoryItems();
  const setStockMutation = useSetStock();
  const [editingId, setEditingId] = useState(null);
  const [newQty, setNewQty] = useState('');

  const items = data?.content || data || [];

  const handleSetStock = (productId) => {
    setStockMutation.mutate({ productId, quantity: parseInt(newQty) }, {
      onSuccess: () => {
        setEditingId(null);
        setNewQty('');
      }
    });
  };

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Inventory Management</h1>

      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-center w-24">Image</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Product Name</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-center">Available</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-center">Reserved</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-center">Total</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {items.map(item => (
              <tr key={item.productId}>
                <td className="px-6 py-4">
                   <div className="h-10 w-10 bg-gray-100 rounded overflow-hidden mx-auto">
                    {item.productImageUrl ? <img src={item.productImageUrl} className="h-full w-full object-cover" /> : null}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{item.productName}</div>
                  <div className="text-xs text-gray-400">ID: {item.productId}</div>
                </td>
                <td className="px-6 py-4 text-center">
                  <Badge tone={item.availableQuantity > 0 ? 'success' : 'danger'}>
                    {item.availableQuantity}
                  </Badge>
                </td>
                <td className="px-6 py-4 text-center text-sm text-gray-500">{item.reservedQuantity}</td>
                <td className="px-6 py-4 text-center text-sm font-bold">{item.totalQuantity}</td>
                <td className="px-6 py-4 text-right">
                  {editingId === item.productId ? (
                    <div className="flex gap-2 justify-end">
                      <input 
                        type="number"
                        className="w-20 border rounded px-2 py-1 text-sm"
                        value={newQty}
                        onChange={(e) => setNewQty(e.target.value)}
                        autoFocus
                      />
                      <Button size="xs" onClick={() => handleSetStock(item.productId)} disabled={setStockMutation.isPending}>Save</Button>
                      <Button size="xs" variant="ghost" onClick={() => setEditingId(null)}>Cancel</Button>
                    </div>
                  ) : (
                    <Button variant="outline" size="xs" onClick={() => { setEditingId(item.productId); setNewQty(item.totalQuantity); }}>Adjust Stock</Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminInventory;
