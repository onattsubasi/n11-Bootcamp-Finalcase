#!/usr/bin/env bash
set -euo pipefail

SERVICE="${1:-}"

if [ -z "$SERVICE" ]; then
  echo "Usage: ./scripts/debug-service.sh <compose-service-or-container-name>"
  echo "Example: ./scripts/debug-service.sh checkout-service"
  echo "Example: ./scripts/debug-service.sh finalcase-checkout-service"
  exit 1
fi

echo "== Docker Compose service status =="
docker compose ps "$SERVICE" || docker ps --filter "name=$SERVICE"

echo
echo "== Last 150 log lines =="
docker compose logs --tail=150 "$SERVICE" 2>/dev/null || docker logs --tail=150 "$SERVICE"

echo
echo "== Container inspect: state/health =="
CONTAINER_ID="$(docker compose ps -q "$SERVICE" 2>/dev/null || true)"
if [ -z "$CONTAINER_ID" ]; then
  CONTAINER_ID="$(docker ps -aqf "name=$SERVICE" || true)"
fi

if [ -n "$CONTAINER_ID" ]; then
  docker inspect "$CONTAINER_ID" --format '{{json .State}}' | python -m json.tool || true

  echo
  echo "== Environment keys (secret values hidden) =="
  docker inspect "$CONTAINER_ID" --format '{{range .Config.Env}}{{println .}}{{end}}' \
    | sed -E 's/(PASSWORD|SECRET|TOKEN|KEY)=.*/\1=***hidden***/g' \
    | grep -E 'SPRING|EUREKA|RABBITMQ|POSTGRES|DATASOURCE|JWT|IYZICO|PAYMENT|OTEL|MANAGEMENT|JAVA_TOOL' || true
else
  echo "Container not found for service/name: $SERVICE"
fi
