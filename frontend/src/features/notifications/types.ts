export type NotificationType =
  | 'WELCOME'
  | 'ORDER_CONFIRMED'
  | 'ORDER_CANCELLED'
  | 'ORDER_PAID'
  | 'ORDER_PAYMENT_FAILED'
  | 'SHIPMENT_CREATED'
  | 'SHIPMENT_SHIPPED'
  | 'SHIPMENT_DELIVERED'
  | 'COUPON_ASSIGNED'
  | 'SYSTEM'
  | string;

export interface Notification {
  id: string;
  notificationId?: string;
  title: string;
  message: string;
  body?: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
  metadata?: Record<string, unknown>;
  [key: string]: unknown;
}

export interface UnreadCountResponse {
  count: number;
  [key: string]: unknown;
}
