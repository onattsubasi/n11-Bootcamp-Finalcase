import { Link } from "react-router-dom";
import { Package, Truck, MapPin, Calendar, AlertCircle } from "lucide-react";

import { useMyShipments } from "@/features/shipment/hooks/useShipment";
import { formatDateTime } from "@/lib/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";

type Shipment = {
  id?: string;
  shipmentId?: string;
  orderId?: string;
  orderNumber?: string;
  status?: string;
  carrier?: string;
  trackingNumber?: string;
  trackingUrl?: string;
  recipientName?: string;
  deliveryAddress?: string;
  estimatedDeliveryDate?: string;
  shippedAt?: string;
  deliveredAt?: string;
  createdAt?: string;
};

function normalizeArray<T>(payload: unknown): T[] {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (!payload || typeof payload !== "object") {
    return [];
  }

  const source = payload as Record<string, unknown>;

  if (Array.isArray(source.data)) {
    return source.data as T[];
  }

  if (source.data && typeof source.data === "object") {
    const data = source.data as Record<string, unknown>;

    if (Array.isArray(data.items)) {
      return data.items as T[];
    }

    if (Array.isArray(data.content)) {
      return data.content as T[];
    }
  }

  if (Array.isArray(source.items)) {
    return source.items as T[];
  }

  if (Array.isArray(source.content)) {
    return source.content as T[];
  }

  return [];
}

function getShipmentId(shipment: Shipment, index: number) {
  return (
    shipment.id ??
    shipment.shipmentId ??
    `${shipment.orderId ?? "shipment"}-${index}`
  );
}

export default function CustomerShipments() {
  const { data, isPending, isError } = useMyShipments();

  const shipments = normalizeArray<Shipment>(data);

  if (isPending) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-10">
        <div className="rounded-3xl border bg-card p-8 shadow-sm">
          <p className="text-sm font-semibold text-muted-foreground">
            Loading shipments...
          </p>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-10">
        <div className="rounded-3xl border border-destructive/30 bg-destructive/5 p-8">
          <div className="flex items-center gap-3 text-destructive">
            <AlertCircle className="h-5 w-5" />
            <p className="font-semibold">Failed to load shipments.</p>
          </div>
        </div>
      </div>
    );
  }

  if (shipments.length === 0) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-10">
        <div className="rounded-3xl border bg-card p-10 text-center shadow-sm">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-muted">
            <Package className="h-7 w-7 text-muted-foreground" />
          </div>
          <h1 className="text-2xl font-black tracking-tight">
            No shipments yet
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Your shipment information will appear here after an order is
            shipped.
          </p>
          <Button asChild className="mt-6">
            <Link to="/orders">View orders</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <div className="mb-8">
        <p className="text-xs font-black uppercase tracking-[0.3em] text-primary">
          Customer Area
        </p>
        <h1 className="mt-2 text-3xl font-black tracking-tight">
          My Shipments
        </h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Track your recent shipments and delivery status.
        </p>
      </div>

      <div className="grid gap-4">
        {shipments.map((shipment, index) => {
          const shipmentId = getShipmentId(shipment, index);

          return (
            <article
              key={shipmentId}
              className="rounded-3xl border bg-card p-6 shadow-sm transition hover:shadow-md"
            >
              <div className="flex flex-col gap-5 md:flex-row md:items-start md:justify-between">
                <div className="flex gap-4">
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary/10">
                    <Truck className="h-6 w-6 text-primary" />
                  </div>

                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="font-black tracking-tight">
                        Shipment #{shipmentId}
                      </h2>
                      {shipment.status ? (
                        <Badge variant="secondary">{shipment.status}</Badge>
                      ) : null}
                    </div>

                    <div className="mt-3 grid gap-2 text-sm text-muted-foreground">
                      {shipment.orderNumber || shipment.orderId ? (
                        <div className="flex items-center gap-2">
                          <Package className="h-4 w-4" />
                          <span>
                            Order: {shipment.orderNumber ?? shipment.orderId}
                          </span>
                        </div>
                      ) : null}

                      {shipment.carrier || shipment.trackingNumber ? (
                        <div className="flex items-center gap-2">
                          <Truck className="h-4 w-4" />
                          <span>
                            {shipment.carrier ?? "Carrier"}
                            {shipment.trackingNumber
                              ? ` · ${shipment.trackingNumber}`
                              : ""}
                          </span>
                        </div>
                      ) : null}

                      {shipment.deliveryAddress ? (
                        <div className="flex items-center gap-2">
                          <MapPin className="h-4 w-4" />
                          <span>{shipment.deliveryAddress}</span>
                        </div>
                      ) : null}

                      {shipment.estimatedDeliveryDate ||
                      shipment.shippedAt ||
                      shipment.createdAt ? (
                        <div className="flex items-center gap-2">
                          <Calendar className="h-4 w-4" />
                          <span>
                            {formatDateTime(
                              shipment.estimatedDeliveryDate ??
                                shipment.shippedAt ??
                                shipment.createdAt,
                            )}
                          </span>
                        </div>
                      ) : null}
                    </div>
                  </div>
                </div>

                <div className="flex gap-2">
                  {shipment.trackingUrl ? (
                    <Button asChild variant="outline" size="sm">
                      <a
                        href={shipment.trackingUrl}
                        target="_blank"
                        rel="noreferrer"
                      >
                        Track
                      </a>
                    </Button>
                  ) : null}

                  {shipment.orderId ? (
                    <Button asChild variant="ghost" size="sm">
                      <Link to={`/orders/${shipment.orderId}`}>
                        Order detail
                      </Link>
                    </Button>
                  ) : null}
                </div>
              </div>
            </article>
          );
        })}
      </div>
    </div>
  );
}
