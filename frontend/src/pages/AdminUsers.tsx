import { useState } from 'react';
import { useAdminUsers, useActivateUser, useDisableUser } from '../features/admin/hooks/useAdmin';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { formatDateTime } from '../lib/utils/format';
import toast from 'react-hot-toast';

const AdminUsers = () => {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useAdminUsers(page, 20);
  const activateMutation = useActivateUser();
  const disableMutation = useDisableUser();

  const users = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const handleToggleStatus = (user) => {
    const isActivating = !user.enabled;
    const action = isActivating ? activateMutation : disableMutation;
    
    action.mutate(user.id, {
      onSuccess: () => toast.success(`User ${isActivating ? 'activated' : 'disabled'} successfully`),
      onError: (err) => toast.error(err.message || 'Action failed')
    });
  };

  return (
    <div className="p-6 space-y-6">
      <header className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
      </header>

      {isLoading ? (
        <div className="flex justify-center p-20"><Spinner /></div>
      ) : (
        <div className="bg-white shadow rounded-lg overflow-hidden border">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider font-medium">
              <tr>
                <th className="px-6 py-3 text-left">User</th>
                <th className="px-6 py-3 text-left">Roles</th>
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">Registered</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-col">
                      <span className="text-sm font-semibold text-gray-900">{user.email}</span>
                      <span className="text-xs text-gray-500">ID: {user.id}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex gap-1 flex-wrap">
                      {user.roles?.map(role => (
                        <Badge key={role} tone="info" className="text-[10px] py-0">{role}</Badge>
                      ))}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Badge tone={user.enabled ? 'success' : 'danger'}>
                      {user.enabled ? 'Active' : 'Disabled'}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDateTime(user.createdAt)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Button
                      variant={user.enabled ? 'outline' : 'primary'}
                      size="sm"
                      onClick={() => handleToggleStatus(user)}
                      disabled={activateMutation.isPending || disableMutation.isPending}
                    >
                      {user.enabled ? 'Disable' : 'Activate'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {!users.length ? <div className="p-10 text-center text-gray-500">No users found.</div> : null}

          {totalPages > 1 && (
            <div className="px-6 py-4 bg-gray-50 border-t flex justify-center gap-2">
              {[...Array(totalPages)].map((_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`px-3 py-1 text-sm rounded ${page === i ? 'bg-blue-600 text-white shadow' : 'bg-white border hover:bg-gray-100'}`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminUsers;
