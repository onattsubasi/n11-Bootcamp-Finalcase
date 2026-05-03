import { useState } from 'react';
import { toast } from 'react-hot-toast';
import { useAddresses, useDeleteAddress, useSetDefaultShipping, useSetDefaultBilling } from '../hooks/useAddresses';
import AddressForm from './AddressForm';
import Button from '../../../components/ui/Button';

const AddressBook = () => {
  const { data, isLoading, isError, error } = useAddresses();
  const deleteMutation = useDeleteAddress();
  const defaultShippingMutation = useSetDefaultShipping();
  const defaultBillingMutation = useSetDefaultBilling();

  const [editingAddress, setEditingAddress] = useState(null);
  const [isAddingNew, setIsAddingNew] = useState(false);

  if (isLoading) return <div className="p-4">Loading address book...</div>;
  if (isError) return <div className="p-4 text-red-500">Error: {error.message}</div>;

  const addresses = data?.content || data || [];

  const handleAction = async (actionFn, addressId, successText) => {
    try {
      await actionFn.mutateAsync(addressId);
      toast.success(successText);
    } catch (err) {
      toast.error(`Failed: ${err.message}`);
    }
  };

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this address?')) {
      handleAction(deleteMutation, id, 'Address deleted.');
    }
  };

  const handleFormFinish = () => {
    setEditingAddress(null);
    setIsAddingNew(false);
    toast.success('Address saved successfully.');
  };

  return (
    <div className="bg-white p-6 rounded shadow">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Address Book</h2>
        {!isAddingNew && (
          <Button
            onClick={() => {
              setEditingAddress(null);
              setIsAddingNew(true);
            }}
            variant="primary"
            size="sm"
          >
            + Add New Address
          </Button>
        )}
      </div>

      {isAddingNew || editingAddress ? (
        <div className="mb-6">
          <AddressForm
            initialData={editingAddress}
            onCancel={() => {
              setIsAddingNew(false);
              setEditingAddress(null);
            }}
            onSuccess={handleFormFinish}
          />
        </div>
      ) : null}

      {!isAddingNew && !editingAddress ? (
        addresses.length === 0 ? (
          <div className="text-gray-500">You have no saved addresses.</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {addresses.map((addr) => (
              <div key={addr.id} className="border p-4 rounded relative hover:shadow-md transition">
                {addr.isDefaultShipping ? <span className="absolute top-2 right-2 bg-green-100 text-green-800 text-xs px-2 py-1 rounded">Default Shipping</span> : null}
                {addr.isDefaultBilling ? <span className="absolute top-8 right-2 bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded">Default Billing</span> : null}

                <div className="font-bold mb-1">{addr.title || 'Address'}</div>
                <div className="text-sm text-gray-700 mb-3">
                  <p>{addr.recipientName}</p>
                  <p>{addr.line1}</p>
                  {addr.line2 ? <p>{addr.line2}</p> : null}
                  <p>{addr.district}, {addr.city}</p>
                  <p>{addr.postalCode} {addr.country}</p>
                  <p className="mt-1 text-gray-500">{addr.phoneNumber}</p>
                </div>

                <div className="flex flex-wrap gap-2 text-sm mt-4 border-t pt-3">
                  <button onClick={() => setEditingAddress(addr)} className="text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(addr.id)} className="text-red-600 hover:underline">Delete</button>
                  {!addr.isDefaultShipping ? (
                    <button onClick={() => handleAction(defaultShippingMutation, addr.id, 'Set as default shipping.')} className="text-gray-600 hover:underline">Set as Shipping</button>
                  ) : null}
                  {!addr.isDefaultBilling ? (
                    <button onClick={() => handleAction(defaultBillingMutation, addr.id, 'Set as default billing.')} className="text-gray-600 hover:underline">Set as Billing</button>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        )
      ) : null}
    </div>
  );
};

export default AddressBook;
