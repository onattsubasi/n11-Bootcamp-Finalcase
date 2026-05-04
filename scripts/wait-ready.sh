#!/usr/bin/env bash
set -euo pipefail

TIMEOUT_SECONDS="${1:-420}"
START_TS="$(date +%s)"

services=(
  "postgres"
  "rabbitmq"
  "discovery-service"
  "api-gateway"
  "auth-service"
  "user-service"
  "catalog-service"
  "search-service"
  "basket-service"
  "inventory-service"
  "promotion-service"
  "order-service"
  "payment-service"
  "shipment-service"
  "checkout-service"
  "notification-service"
  "review-service"
)

echo "Waiting up to ${TIMEOUT_SECONDS}s for Finalcase services to become healthy..."

while true; do
  now="$(date +%s)"
  elapsed="$((now - START_TS))"

  if [ "$elapsed" -gt "$TIMEOUT_SECONDS" ]; then
    echo "Timed out waiting for services."
    docker compose ps
    exit 1
  fi

  unhealthy=0

  for service in "${services[@]}"; do
    cid="$(docker compose ps -q "$service" 2>/dev/null || true)"
    if [ -z "$cid" ]; then
      echo "[$elapsed s] $service: not created yet"
      unhealthy=1
      continue
    fi

    status="$(docker inspect "$cid" --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' 2>/dev/null || echo unknown)"
    if [ "$status" != "healthy" ] && [ "$status" != "running" ]; then
      echo "[$elapsed s] $service: $status"
      unhealthy=1
    fi
  done

  if [ "$unhealthy" -eq 0 ]; then
    echo "All expected services are healthy/running."
    echo
    echo "Useful local links:"
    echo "API Gateway: http://localhost:${GATEWAY_PORT:-8080}"
    echo "Eureka:      http://localhost:${EUREKA_PORT:-8761}"
    echo "RabbitMQ:    http://localhost:${RABBITMQ_MANAGEMENT_PORT:-15672}"
    echo "Prometheus:  http://localhost:${PROMETHEUS_PORT:-9090}"
    echo "Grafana:     http://localhost:${GRAFANA_PORT:-3000}  admin/admin"
    echo "Jaeger:      http://localhost:${JAEGER_UI_PORT:-16686}"
    exit 0
  fi

  echo "Still waiting..."
  sleep 10
done
