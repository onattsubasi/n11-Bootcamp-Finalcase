import { useState } from 'react';
import { toast } from 'react-hot-toast';
import { useCreateAddress, useUpdateAddress } from '../hooks/useAddresses';
import Button from '../../../components/ui/Button';
import Input from '../../../components/ui/Input';

const AddressForm = ({ initialData = null, onCancel, onSuccess }) => {
  const createMutation = useCreateAddress();
  const updateMutation = useUpdateAddress();
  
  const [formData, setFormData] = useState({
    title: initialData?.title || '',
    type: initialData?.type || 'BOTH',
    recipientName: initialData?.recipientName || '',
    phoneNumber: initialData?.phoneNumber || '',
    line1: initialData?.line1 || '',
    line2: initialData?.line2 || '',
    district: initialData?.district || '',
    city: initialData?.city || '',
    country: initialData?.country || 'Turkey',
    postalCode: initialData?.postalCode || '',
    defaultShipping: initialData?.defaultShipping || false,
    defaultBilling: initialData?.defaultBilling || false,
  });

  const isEditing = !!initialData;
  const isPending = createMutation.isPending || updateMutation.isPending;

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({ 
      ...prev, 
      [name]: type === 'checkbox' ? checked : value 
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const action = isEditing 
      ? updateMutation.mutateAsync({ addressId: initialData.id, addressData: formData })
      : createMutation.mutateAsync(formData);

    action
      .then(() => {
        if (onSuccess) onSuccess();
      })
      .catch((err) => {
        toast.error(err.message || 'Failed to save address.');
      });
  };

  return (
    <form onSubmit={handleSubmit} className="border p-4 rounded bg-gray-50 space-y-4">
      <h3 className="font-bold text-lg mb-2">{isEditing ? 'Edit Address' : 'Add New Address'}</h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium mb-1">Address Title (e.g. Home, Work)</label>
          <Input type="text" name="title" value={formData.title} onChange={handleChange} placeholder="Home" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Address Type</label>
          <select 
            name="type" 
            value={formData.type} 
            onChange={handleChange}
            className="w-full p-2 border rounded bg-white text-sm"
          >
            <option value="BOTH">Both</option>
            <option value="SHIPPING">Shipping Only</option>
            <option value="BILLING">Billing Only</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Recipient Name</label>
          <Input type="text" name="recipientName" value={formData.recipientName} onChange={handleChange} placeholder="Full name" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Phone Number</label>
          <Input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} placeholder="+1 (555) 123-4567" required />
        </div>
        <div className="md:col-span-2">
          <label className="block text-sm font-medium mb-1">Address Line 1</label>
          <Input type="text" name="line1" value={formData.line1} onChange={handleChange} placeholder="Street address" required />
        </div>
        <div className="md:col-span-2">
          <label className="block text-sm font-medium mb-1">Address Line 2 (Optional)</label>
          <Input type="text" name="line2" value={formData.line2} onChange={handleChange} placeholder="Apartment, suite, etc." />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">District</label>
          <Input type="text" name="district" value={formData.district} onChange={handleChange} placeholder="District" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">City</label>
          <Input type="text" name="city" value={formData.city} onChange={handleChange} placeholder="City" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Zip / Postal Code</label>
          <Input type="text" name="postalCode" value={formData.postalCode} onChange={handleChange} placeholder="Zip code" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Country</label>
          <Input type="text" name="country" value={formData.country} onChange={handleChange} placeholder="Country" required />
        </div>
      </div>

      <div className="flex space-x-6 py-2 border-t border-b">
        <label className="flex items-center space-x-2 cursor-pointer">
          <input type="checkbox" name="defaultShipping" checked={formData.defaultShipping} onChange={handleChange} className="rounded text-primary focus:ring-primary" />
          <span className="text-sm">Default Shipping</span>
        </label>
        <label className="flex items-center space-x-2 cursor-pointer">
          <input type="checkbox" name="defaultBilling" checked={formData.defaultBilling} onChange={handleChange} className="rounded text-primary focus:ring-primary" />
          <span className="text-sm">Default Billing</span>
        </label>
      </div>

      <div className="flex justify-end space-x-2 pt-2">
        <Button type="button" onClick={onCancel} variant="secondary" disabled={isPending}>
          Cancel
        </Button>
        <Button type="submit" variant="primary" disabled={isPending}>
          {isPending ? 'Saving...' : 'Save'}
        </Button>
      </div>
    </form>
  );
};

export default AddressForm;
