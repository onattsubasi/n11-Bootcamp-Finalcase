import { useState } from 'react';
import { 
  useProductLists, 
  useCreateProductList, 
  useDeleteProductList,
  useAddProductListItem,
  useRemoveProductListItem
} from '../hooks/useUserHooks';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Spinner } from '../../../components/ui/Spinner';
import { Badge } from '../../../components/ui/Badge';
import { formatCurrency } from '../../../lib/utils/format';
import { Trash2, Plus, List as ListIcon, ChevronRight } from 'lucide-react';
import toast from 'react-hot-toast';

const ProductListsTab = () => {
  const { data: lists, isPending, isError } = useProductLists();
  const createMutation = useCreateProductList();
  const deleteMutation = useDeleteProductList();
  const removeItemMutation = useRemoveProductListItem();
  
  const [newListName, setNewListName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [selectedListId, setSelectedListId] = useState(null);

  const handleCreateList = (e) => {
    e.preventDefault();
    if (!newListName.trim()) return;
    
    createMutation.mutate({ name: newListName, description: '', visibility: 'PRIVATE' }, {
      onSuccess: () => {
        setNewListName('');
        setIsCreating(false);
        toast.success('List created!');
      }
    });
  };

  const handleDeleteList = (id, e) => {
    e.stopPropagation();
    if (window.confirm('Delete this list?')) {
      deleteMutation.mutate(id);
      if (selectedListId === id) setSelectedListId(null);
    }
  };

  const handleRemoveItem = (listId, productId, e) => {
    e.stopPropagation();
    removeItemMutation.mutate({ listId, productId }, {
      onSuccess: () => toast.success('Item removed')
    });
  };

  if (isPending) return <div className="p-8 flex justify-center"><Spinner /></div>;
  if (isError) return <div className="p-8 text-red-500">Failed to load lists.</div>;

  const selectedList = lists?.find(l => l.id === selectedListId);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Sidebar: List of Lists */}
      <div className="lg:col-span-1 space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="font-bold text-gray-900">My Lists</h3>
          <Button size="xs" onClick={() => setIsCreating(true)} variant="ghost">
            <Plus className="h-4 w-4 mr-1" /> New
          </Button>
        </div>

        {isCreating && (
          <Card className="p-3">
            <form onSubmit={handleCreateList} className="space-y-2">
              <input 
                autoFocus
                className="w-full border rounded px-2 py-1 text-sm outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="List name..."
                value={newListName}
                onChange={(e) => setNewListName(e.target.value)}
              />
              <div className="flex gap-1 justify-end">
                <Button size="xs" variant="ghost" onClick={() => setIsCreating(false)}>Cancel</Button>
                <Button size="xs" type="submit" disabled={createMutation.isPending}>Create</Button>
              </div>
            </form>
          </Card>
        )}

        <div className="space-y-2">
          {lists?.map(list => (
            <div 
              key={list.id}
              onClick={() => setSelectedListId(list.id)}
              className={`p-3 rounded-lg border cursor-pointer transition-all flex justify-between items-center ${selectedListId === list.id ? 'border-blue-500 bg-blue-50 shadow-sm' : 'bg-white border-gray-100 hover:border-gray-200'}`}
            >
              <div className="flex items-center gap-2 overflow-hidden">
                <ListIcon className="h-4 w-4 text-gray-400 flex-shrink-0" />
                <div className="min-w-0">
                  <div className="text-sm font-medium truncate">{list.name}</div>
                  <div className="flex items-center gap-1.5">
                    <span className="text-[10px] uppercase font-bold text-gray-400">{list.visibility || 'PRIVATE'}</span>
                    <span className="text-[10px] text-gray-300">•</span>
                    <span className="text-[10px] text-gray-400">{list.items?.length || 0} items</span>
                  </div>
                </div>
              </div>
              <button 
                onClick={(e) => handleDeleteList(list.id, e)}
                className="text-gray-300 hover:text-red-500 transition-colors"
              >
                <Trash2 className="h-3.5 w-3.5" />
              </button>
            </div>
          ))}
          {!lists?.length && !isCreating && <p className="text-sm text-gray-400 text-center py-4">No lists yet.</p>}
        </div>
      </div>

      {/* Main Content: List Details */}
      <div className="lg:col-span-2">
        {!selectedListId ? (
          <div className="h-full flex flex-col items-center justify-center p-12 bg-gray-50 rounded-xl border border-dashed text-gray-400">
            <ListIcon className="h-12 w-12 mb-3 opacity-20" />
            <p>Select a list to view items</p>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="flex justify-between items-end border-b pb-4">
              <div>
                <h2 className="text-xl font-bold text-gray-900">{selectedList.name}</h2>
                <p className="text-sm text-gray-500">{selectedList.description || 'Custom collection'}</p>
              </div>
              <Badge tone="info">{selectedList.items?.length || 0} items</Badge>
            </div>

            <div className="space-y-3">
              {selectedList.items?.map(item => (
                <Card key={item.productId} className="p-3 flex gap-4 items-center group">
                  <div className="h-16 w-16 bg-gray-100 rounded overflow-hidden flex-shrink-0">
                    {item.productImageUrl && <img src={item.productImageUrl} className="h-full w-full object-cover" />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-bold text-gray-900 truncate">{item.productName}</h4>
                    <p className="text-xs text-gray-500 truncate">{item.note || 'Added to list'}</p>
                    <div className="mt-1 font-semibold text-sm text-blue-600">{formatCurrency(item.price || 0)}</div>
                  </div>
                  <div className="flex gap-2">
                     <Button size="xs" variant="outline" className="opacity-0 group-hover:opacity-100 transition-opacity">
                      View
                    </Button>
                    <button 
                      onClick={(e) => handleRemoveItem(selectedListId, item.productId, e)}
                      className="p-1.5 text-gray-300 hover:text-red-500 transition-colors"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </Card>
              ))}
              {!selectedList.items?.length && (
                <div className="py-12 text-center text-gray-400">
                  This list is empty.
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductListsTab;
