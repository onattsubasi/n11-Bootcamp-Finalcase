import { useNotifications, useMarkNotificationRead } from '../features/notifications/hooks/useNotifications';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { Card } from '../components/ui/Card';
import { formatDateTime } from '../lib/utils/format';
import { Bell, Check, Inbox, AlertTriangle, CheckCircle, Info } from 'lucide-react';
import { cn } from '../lib/utils/cn';
import toast from 'react-hot-toast';

const TYPE_CONFIG = {
  WELCOME: { icon: Info, color: 'text-blue-500', bg: 'bg-blue-50' },
  ORDER_CONFIRMED: { icon: CheckCircle, color: 'text-green-500', bg: 'bg-green-50' },
  ORDER_CANCELLED: { icon: AlertTriangle, color: 'text-red-500', bg: 'bg-red-50' },
  SYSTEM: { icon: Bell, color: 'text-gray-500', bg: 'bg-gray-50' },
};

const CustomerNotifications = () => {
  const { data, isLoading, isError } = useNotifications(0, 50);
  const markReadMutation = useMarkNotificationRead();

  const notifications = data?.items ?? data?.content ?? [];

  const handleMarkRead = (id) => {
    markReadMutation.mutate(id, {
      onSuccess: () => toast.success('Marked as read'),
    });
  };

  if (isLoading) return <div className="flex justify-center p-20"><Spinner size="lg" /></div>;
  if (isError) return <div className="p-8 text-red-500 text-center">Failed to load notifications.</div>;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <Inbox className="h-6 w-6 text-blue-600" />
          Notifications
        </h1>
        <Badge tone="info">{notifications.filter(n => !n.read).length} Unread</Badge>
      </div>

      <div className="space-y-3">
        {notifications.map((n) => {
          const config = TYPE_CONFIG[n.type] || TYPE_CONFIG.SYSTEM;
          const Icon = config.icon;

          return (
            <Card 
              key={n.notificationId || n.id} 
              className={cn(
                "p-4 flex gap-4 items-start transition-all border-l-4",
                n.read ? "border-transparent opacity-75" : "border-blue-500 bg-blue-50/30 shadow-sm"
              )}
            >
              <div className={cn("p-2 rounded-full", config.bg)}>
                <Icon className={cn("h-5 w-5", config.color)} />
              </div>
              
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start">
                  <h3 className={cn("font-bold text-gray-900 truncate", !n.read && "text-blue-900")}>
                    {n.title}
                  </h3>
                  <span className="text-xs text-gray-400 whitespace-nowrap ml-2">
                    {formatDateTime(n.createdAt)}
                  </span>
                </div>
                <p className="mt-1 text-sm text-gray-600 line-clamp-2">{n.body}</p>
                
                {!n.read && (
                  <button 
                    onClick={() => handleMarkRead(n.notificationId || n.id)}
                    className="mt-2 text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1"
                  >
                    <Check className="h-3 w-3" /> Mark as read
                  </button>
                )}
              </div>
            </Card>
          );
        })}

        {notifications.length === 0 && (
          <div className="py-20 text-center bg-white rounded-xl border border-dashed">
            <Bell className="h-12 w-12 text-gray-200 mx-auto mb-3" />
            <p className="text-gray-500 font-medium">Your inbox is empty</p>
            <p className="text-sm text-gray-400 mt-1">We'll notify you when something important happens.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerNotifications;
