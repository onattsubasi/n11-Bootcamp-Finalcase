import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as api from '../api/notificationApi';
import type { Notification, UnreadCountResponse } from '../types';

type AnyRecord = Record<string, any>;

export type NotificationCollection = Notification[] & {
  items: Notification[];
  content: Notification[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
};

const normalizeNotification = (raw: AnyRecord): Notification => {
  const id = String(raw.notificationId ?? raw.id ?? raw.notification_id ?? '');
  const message = raw.message ?? raw.body ?? raw.content ?? raw.description ?? '';

  return {
    ...raw,
    id,
    notificationId: raw.notificationId ?? raw.id ?? id,
    title: raw.title ?? raw.subject ?? 'Notification',
    message,
    body: raw.body ?? message,
    type: raw.type ?? raw.notificationType ?? 'SYSTEM',
    read: Boolean(raw.read ?? raw.isRead ?? raw.readAt ?? raw.status === 'READ'),
    createdAt: raw.createdAt ?? raw.created_at ?? raw.sentAt ?? new Date().toISOString(),
    metadata: raw.metadata ?? {},
  } as Notification;
};

const normalizeCollection = (payload: any): NotificationCollection => {
  const source = payload?.data ?? payload ?? {};
  const rawItems = Array.isArray(source)
    ? source
    : source.items ?? source.content ?? source.notifications ?? [];

  const normalizedItems = Array.isArray(rawItems)
    ? rawItems.map((item: AnyRecord) => normalizeNotification(item))
    : [];

  const collection = normalizedItems as NotificationCollection;
  collection.items = normalizedItems;
  collection.content = normalizedItems;
  collection.totalElements = source.totalElements ?? source.total ?? normalizedItems.length;
  collection.totalPages = source.totalPages ?? source.pages ?? 1;
  collection.page = source.page?.page ?? source.page ?? 0;
  collection.size = source.page?.size ?? source.size ?? normalizedItems.length;

  return collection;
};

const normalizeUnreadCount = (payload: any): UnreadCountResponse => {
  const source = payload?.data ?? payload ?? {};
  const count = source.count ?? source.unreadCount ?? source.totalUnread ?? 0;
  return { ...source, count } as UnreadCountResponse;
};

export const useNotifications = (page = 0, size = 20, unreadOnly = false) => {
  return useQuery({
    queryKey: ['notifications', page, size, unreadOnly],
    queryFn: async () => normalizeCollection(await api.fetchNotifications(page, size, unreadOnly)),
  });
};

export const useUnreadCount = () => {
  return useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => normalizeUnreadCount(await api.fetchUnreadCount()),
    refetchInterval: 30_000,
  });
};

export const useMarkRead = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useMarkNotificationRead = useMarkRead;

export const useMarkAllRead = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (_?: unknown) => api.markAllRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useMarkAllNotificationsRead = useMarkAllRead;
