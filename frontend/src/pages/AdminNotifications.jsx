import { useAdminNotifications } from '../features/notifications/hooks/useNotificationAdmin';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { formatDateTime } from '../lib/utils/format';

const AdminNotifications = () => {
  const { data, isLoading } = useAdminNotifications();
  const notifications = data?.content || data?.items || data || [];

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Manage Notifications</h1>
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">User ID</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Type</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Title</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Created</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {notifications.map(n => (
              <tr key={n.id || n.notificationId}>
                <td className="px-6 py-4 text-sm text-gray-500 font-mono">{(n.id || n.notificationId)?.substring(0, 8)}...</td>
                <td className="px-6 py-4 text-sm text-gray-900">{n.userId}</td>
                <td className="px-6 py-4 text-sm text-gray-500">{n.type}</td>
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{n.title}</td>
                <td className="px-6 py-4">
                  <Badge tone={n.read ? 'success' : 'warning'}>{n.read ? 'Read' : 'Unread'}</Badge>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{formatDateTime(n.createdAt)}</td>
              </tr>
            ))}
            {!notifications.length && (
              <tr>
                <td colSpan="6" className="px-6 py-8 text-center text-gray-400">No notifications found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminNotifications;
