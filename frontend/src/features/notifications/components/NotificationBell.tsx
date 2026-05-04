import React, { useState, useRef, useEffect } from 'react';
import { Bell } from 'lucide-react';
import { useUnreadCount, useNotifications, useMarkRead } from '../hooks/useNotifications';
import { useStore } from '@/store';
import { formatDate } from '@/lib/utils/format';
import { cn } from '@/lib/utils/cn';
import { NotificationType } from '../types';

const ICONS: Record<NotificationType, string> = {
  WELCOME: '👋',
  ORDER_CONFIRMED: '✅',
  ORDER_CANCELLED: '⚠️',
  SYSTEM: 'ℹ️',
};

export const NotificationBell: React.FC = () => {
  const isAuthenticated = useStore(state => state.isAuthenticated);
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const { data: unreadData } = useUnreadCount();
  const { data: notifications = [], isLoading } = useNotifications(0, 5);
  const { mutate: markRead } = useMarkRead();

  const unreadCount = unreadData?.count || 0;

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!isAuthenticated) return null;

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={cn(
          "relative flex h-10 w-10 items-center justify-center rounded-xl transition-all duration-300",
          isOpen ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-accent hover:text-foreground"
        )}
      >
        <Bell className={cn("h-5 w-5", unreadCount > 0 && "animate-wiggle")} />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-black text-white ring-2 ring-background">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 z-50 mt-3 w-80 overflow-hidden rounded-[2rem] border border-border bg-card shadow-2xl animate-in fade-in zoom-in-95 duration-200">
          <div className="flex items-center justify-between border-b border-border/50 px-6 py-4">
            <span className="text-xs font-black uppercase tracking-widest text-foreground">Notifications</span>
            {unreadCount > 0 && (
              <span className="rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-bold text-primary uppercase">
                {unreadCount} New
              </span>
            )}
          </div>

          <div className="max-h-[30rem] overflow-y-auto no-scrollbar">
            {isLoading ? (
              <div className="flex flex-col items-center justify-center py-10 gap-2">
                <div className="h-5 w-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Syncing...</span>
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 px-6 text-center gap-3">
                <div className="text-4xl opacity-20">📭</div>
                <div>
                  <p className="text-xs font-black text-foreground uppercase tracking-widest">Inbox Empty</p>
                  <p className="text-[11px] text-muted-foreground mt-1">We'll let you know when something important happens.</p>
                </div>
              </div>
            ) : (
              <div className="divide-y divide-border/30">
                {notifications.map((n) => (
                  <button
                    key={n.id}
                    onClick={() => {
                      if (!n.read) markRead(n.id);
                      // Handle navigation if metadata has URL
                    }}
                    className={cn(
                      'flex w-full gap-4 px-6 py-4 text-left transition-all duration-200 hover:bg-accent/50',
                      !n.read && 'bg-primary/[0.03]'
                    )}
                  >
                    <span className="shrink-0 mt-0.5 text-xl">{ICONS[n.type] || '🔔'}</span>
                    <div className="min-w-0 flex-1 space-y-1">
                      <div className="flex items-start justify-between gap-2">
                        <p className={cn("text-xs font-black leading-tight", !n.read ? "text-foreground" : "text-muted-foreground")}>
                          {n.title}
                        </p>
                        {!n.read && <span className="shrink-0 h-2 w-2 rounded-full bg-primary animate-pulse mt-1" />}
                      </div>
                      <p className="line-clamp-2 text-[11px] text-muted-foreground/80 leading-relaxed font-medium">
                        {n.message}
                      </p>
                      <p className="text-[9px] font-bold text-muted-foreground/40 uppercase tracking-wider pt-1">
                        {formatDate(n.createdAt)}
                      </p>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          <button className="w-full py-4 bg-muted/30 text-[10px] font-black uppercase tracking-[0.2em] text-muted-foreground hover:bg-muted/50 hover:text-foreground transition-all">
            See All Activities
          </button>
        </div>
      )}
    </div>
  );
};
