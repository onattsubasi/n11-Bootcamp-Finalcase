import { useAdminProducts } from '../features/admin/hooks/useAdmin';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Spinner } from '../components/ui/Spinner';
import { Plus, Edit2, Trash2, CheckCircle, XCircle } from 'lucide-react';
import { useActivateProduct, useSuspendProduct } from '../features/admin/hooks/useAdmin';
import toast from 'react-hot-toast';

const AdminProducts = () => {
  const { data, isLoading, isError, refetch } = useAdminProducts(0, 50);
  const activateMutation = useActivateProduct();
  const suspendMutation = useSuspendProduct();

  if (isLoading) return <div className="p-8 flex justify-center"><Spinner /></div>;
  if (isError) return <div className="p-8 text-red-500">Error loading products.</div>;

  const handleToggleStatus = async (product) => {
    try {
      if (product.status === 'ACTIVE') {
        await suspendMutation.mutateAsync(product.id);
        toast.success('Product suspended');
      } else {
        await activateMutation.mutateAsync(product.id);
        toast.success('Product activated');
      }
      refetch();
    } catch {
      toast.error('Failed to update product status');
    }
  };

  const products = data?.content || data?.data?.content || data || [];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Product Catalog</h1>
        <Button size="sm">
          <Plus className="h-4 w-4 mr-1" /> Create Product
        </Button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr className="text-xs font-semibold text-gray-500 uppercase">
              <th className="px-6 py-3">Product</th>
              <th className="px-6 py-3">Price</th>
              <th className="px-6 py-3">Status</th>
              <th className="px-6 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {products.map((p) => (
              <tr key={p.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4">
                  <div className="flex flex-col">
                    <span className="font-semibold text-gray-900">{p.name}</span>
                    <span className="text-xs text-gray-400 font-mono">{p.id}</span>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm font-medium">${p.price}</td>
                <td className="px-6 py-4">
                  <Badge tone={p.status === 'ACTIVE' ? 'success' : 'neutral'}>
                    {p.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 text-right">
                  <div className="flex justify-end gap-1">
                    <button 
                      onClick={() => handleToggleStatus(p)}
                      className={`p-1.5 transition-colors ${p.status === 'ACTIVE' ? 'text-amber-500 hover:text-amber-600' : 'text-emerald-500 hover:text-emerald-600'}`}
                      title={p.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                    >
                      {p.status === 'ACTIVE' ? <XCircle className="h-4 w-4" /> : <CheckCircle className="h-4 w-4" />}
                    </button>
                    <button className="p-1.5 text-gray-400 hover:text-blue-600 transition-colors">
                      <Edit2 className="h-4 w-4" />
                    </button>
                    <button className="p-1.5 text-gray-400 hover:text-red-600 transition-colors">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!products.length && (
          <div className="p-12 text-center text-gray-400">No products found.</div>
        )}
      </div>
    </div>
  );
};

export default AdminProducts;
