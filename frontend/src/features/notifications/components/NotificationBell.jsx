import { useEffect, useRef, useState } from 'react';
import { Bell } from 'lucide-react';
import { useStore } from '../../../store';
import { useMarkNotificationRead, useNotifications } from '../hooks/useNotifications';
import { cn } from '../../../lib/utils/cn';
import { formatDateTime } from '../../../lib/utils/format';

const TYPE_ICONS = {
  WELCOME: '👋',
  ORDER_CONFIRMED: '✅',
  ORDER_CANCELLED: '⚠️',
  SYSTEM: 'ℹ️',
};

export const NotificationBell = () => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);
  const itemsQuery = useNotifications(0, 5, false);
  const unreadQuery = useNotifications(0, 1, true);
  const { mutate: markRead } = useMarkNotificationRead();

  useEffect(() => {
    const handlePointerDown = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    return () => document.removeEventListener('mousedown', handlePointerDown);
  }, []);

  if (!isAuthenticated) {
    return null;
  }

  const notifications = itemsQuery.data?.items ?? [];
  const unreadCount = unreadQuery.data?.unreadCount ?? unreadQuery.data?.items?.length ?? 0;
  const isLoading = itemsQuery.isPending || unreadQuery.isPending;

  const handleMarkRead = (notification) => {
    if (notification.read) {
      return;
    }

    markRead(notification.notificationId, {
      onError: () => {},
    });
  };

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="relative flex h-10 w-10 items-center justify-center rounded-lg transition-colors hover:bg-white/10"
        aria-label="Notifications"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 ? (
          <span className="absolute -right-0.5 -top-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white shadow ring-2 ring-slate-900">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        ) : null}
      </button>

      {open ? (
        <div className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-2xl">
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <span className="text-sm font-semibold text-gray-900">Notifications</span>
            <div className="flex items-center gap-2">
              {unreadCount > 0 ? (
                <span className="rounded-full bg-blue-50 px-2 py-0.5 text-xs font-medium text-blue-700">
                  {unreadCount} unread
                </span>
              ) : null}
              <button 
                onClick={() => { setOpen(false); window.location.href='/notifications'; }}
                className="text-xs text-blue-600 hover:underline font-medium"
              >
                See all
              </button>
            </div>
          </div>

          <div className="max-h-80 overflow-y-auto">
            {isLoading ? (
              <div className="p-6 text-center text-sm text-gray-500">Loading...</div>
            ) : notifications.length === 0 ? (
              <div className="p-8 text-center text-sm text-gray-500">No notifications.</div>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.notificationId}
                  type="button"
                  onClick={() => handleMarkRead(notification)}
                  className={cn(
                    'flex w-full gap-3 border-b border-gray-100 px-4 py-3 text-left transition-colors hover:bg-gray-50',
                    notification.read ? 'bg-white' : 'bg-blue-50/40'
                  )}
                >
                  <span className="mt-0.5 text-lg leading-none">{TYPE_ICONS[notification.type] ?? '•'}</span>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2 text-sm font-medium text-gray-900">
                      <span className="truncate">{notification.title}</span>
                      {notification.read ? null : <span className="h-2 w-2 rounded-full bg-blue-600" />}
                    </div>
                    <p className="mt-0.5 line-clamp-2 text-xs text-gray-600">{notification.body}</p>
                    <p className="mt-1 text-[11px] text-gray-400">{formatDateTime(notification.createdAt)}</p>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      ) : null}
    </div>
  );
};

export default NotificationBell;