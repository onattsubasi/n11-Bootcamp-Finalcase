#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../backend"

modules=(
  "services/discovery-service"
  "services/api-gateway"
  "services/auth-service"
  "services/user-service"
  "services/catalog-service"
  "services/search-service"
  "services/basket-service"
  "services/inventory-service"
  "services/promotion-service"
  "services/checkout-service"
  "services/order-service"
  "services/payment-service"
  "services/shipment-service"
  "services/notification-service"
  "services/review-service"
)

for module in "${modules[@]}"; do
  service="$(basename "$module")"
  echo "Building $service with Jib..."
  ./mvnw -B -ntp -pl "$module" -am compile com.google.cloud.tools:jib-maven-plugin:3.4.5:dockerBuild     -Dimage="finalcase/$service:local"
done
