import { useState } from 'react';
import { toast } from 'react-hot-toast';
import { useChangePassword } from '../../auth/hooks/useChangePassword';
import Button from '../../../components/ui/Button';
import Input from '../../../components/ui/Input';

const ChangePasswordForm = () => {
  const changePasswordMutation = useChangePassword();
  const [formData, setFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [showPasswords, setShowPasswords] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (formData.newPassword !== formData.confirmPassword) {
      toast.error('New passwords do not match.');
      return;
    }

    if (formData.newPassword.length < 8) {
      toast.error('New password must be at least 8 characters.');
      return;
    }

    changePasswordMutation.mutate(
      {
        oldPassword: formData.oldPassword,
        newPassword: formData.newPassword,
      },
      {
        onSuccess: () => {
          setFormData({ oldPassword: '', newPassword: '', confirmPassword: '' });
          toast.success('Password changed successfully!');
        },
        onError: (err) => {
          toast.error(err.message || 'Failed to change password.');
        },
      }
    );
  };

  return (
    <div className="bg-white p-6 rounded shadow">
      <h2 className="text-xl font-bold mb-4">Change Password</h2>

      <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
        <div>
          <label className="block text-sm font-medium mb-1">Current Password</label>
          <Input
            type="password"
            name="oldPassword"
            value={formData.oldPassword}
            onChange={handleChange}
            placeholder="Enter your current password"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">New Password</label>
          <Input
            type={showPasswords ? 'text' : 'password'}
            name="newPassword"
            value={formData.newPassword}
            onChange={handleChange}
            placeholder="Enter new password (min. 8 characters)"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Confirm New Password</label>
          <Input
            type={showPasswords ? 'text' : 'password'}
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="Confirm new password"
            required
          />
        </div>

        <div className="flex items-center">
          <input
            type="checkbox"
            id="showPasswords"
            checked={showPasswords}
            onChange={(e) => setShowPasswords(e.target.checked)}
            className="w-4 h-4 rounded"
          />
          <label htmlFor="showPasswords" className="ml-2 text-sm">
            Show passwords
          </label>
        </div>

        <Button
          type="submit"
          disabled={changePasswordMutation.isPending}
          variant="primary"
        >
          {changePasswordMutation.isPending ? 'Changing...' : 'Change Password'}
        </Button>
      </form>
    </div>
  );
};

export default ChangePasswordForm;
