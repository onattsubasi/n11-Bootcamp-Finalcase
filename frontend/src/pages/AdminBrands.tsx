import { useAdminBrands, useCreateBrand, useUpdateBrand } from '../features/catalog/hooks/useCatalogAdmin';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { useState } from 'react';

const AdminBrands = () => {
  const { data: brands, isLoading } = useAdminBrands();
  const createMutation = useCreateBrand();
  const [newBrandName, setNewBrandName] = useState('');

  const handleCreate = (e) => {
    e.preventDefault();
    if (!newBrandName.trim()) return;
    createMutation.mutate({ name: newBrandName }, {
      onSuccess: () => setNewBrandName('')
    });
  };

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Manage Brands</h1>
      </div>

      <div className="bg-white p-4 rounded-lg shadow-sm border">
        <h3 className="font-semibold mb-3">Add New Brand</h3>
        <form onSubmit={handleCreate} className="flex gap-2">
          <input 
            className="flex-1 border rounded px-3 py-2 outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Brand name (e.g. Nike, Apple)"
            value={newBrandName}
            onChange={(e) => setNewBrandName(e.target.value)}
          />
          <Button type="submit" disabled={createMutation.isPending}>Add Brand</Button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Name</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {brands?.map(brand => (
              <tr key={brand.id}>
                <td className="px-6 py-4 text-sm text-gray-500">{brand.id}</td>
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{brand.name}</td>
                <td className="px-6 py-4 text-right">
                  <Button variant="ghost" size="xs">Edit</Button>
                </td>
              </tr>
            ))}
            {!brands?.length && (
              <tr>
                <td colSpan="3" className="px-6 py-8 text-center text-gray-400">No brands found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminBrands;
