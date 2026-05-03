# Remaining Frontend Implementation Requirements

This document outlines the remaining features and API integrations required to fully align the React SPA with the Backend OpenAPI contracts (`frontend_contract_part1_public_customer.openapi.json` and `frontend_contract_part2_admin_operations.openapi.json`). 

## 1. Architectural & Code Quality Rules
All new implementations **MUST** strictly adhere to the Vercel React Best Practices defined in the project:
*   **Rendering Performance & Re-renders**: 
    *   Use ternary operators (`condition ? true : false`) instead of logical AND (`&&`) to avoid rendering falsy UI artifacts.
    *   NEVER define inline components (e.g., no components inside components).
    *   Use functional state updates where applicable.
*   **Data Fetching**:
    *   Use strictly **TanStack Query** (`useQuery`, `useMutation`) wrapped in custom hooks.
    *   No `useEffect` data fetching.
    *   Maintain strict client/server state separation. Server state in TanStack, client state in Zustand.

## 2. Customer Features (Part 1 - Public & Customer API)

### 2.1. User Profile & Addresses
*   **Endpoints**: 
    *   `GET /api/customer/profile`, `PUT /api/customer/profile`
    *   `GET /api/customer/addresses`, `POST /api/customer/addresses`
    *   `PUT/DELETE /api/customer/addresses/{addressId}`
    *   `POST /api/customer/addresses/{addressId}/default-shipping` / `default-billing`
*   **Components needed**: `ProfileLayout.jsx`, `AddressBook.jsx`, `ProfileForm.jsx`

### 2.2. Favorites & Product Lists
*   **Endpoints**: 
    *   `GET/POST/DELETE /api/customer/favorites`, `/api/customer/favorites/{productId}`
    *   `GET/POST/DELETE /api/customer/product-lists`, `/api/customer/product-lists/{listId}/items/{productId}`
*   **Components needed**: `FavoritesPage.jsx`, `ProductListsPage.jsx`

### 2.3. Customer Coupons & Notifications
*   **Endpoints**: 
    *   `GET /api/customer/coupons`, `POST /api/customer/coupons/{code}/validate`
    *   `GET /api/customer/notifications`, `PATCH /api/customer/notifications/{notificationId}/read`
*   **Components needed**: `MyCoupons.jsx`, `NotificationInbox.jsx`

### 2.4. Customer Reviews
*   **Endpoints**: 
    *   `GET/POST /api/customer/reviews`
    *   `PUT/DELETE /api/customer/reviews/{reviewId}`
*   **Components needed**: `MyReviews.jsx`, `ProductReviewSection.jsx` *(on Product Detail page)*

---

## 3. Admin & Operations Features (Part 2 - Admin API)

### 3.1. Catalog Extended (Categories & Brands)
*   **Endpoints**: 
    *   `GET/POST/PUT/DELETE /api/admin/categories` and `/api/admin/categories/{categoryId}`
    *   `GET/POST/PUT/DELETE /api/admin/brands` and `/api/admin/brands/{brandId}`
*   **Components needed**: `AdminCategories.jsx`, `AdminBrands.jsx`

### 3.2. Inventory Management
*   **Endpoints**:
    *   `GET/POST /api/admin/inventory/items`
    *   `GET/PATCH /api/admin/inventory/items/{inventoryItemId}`
    *   `GET /api/admin/inventory/products/{productId}`
*   **Components needed**: `AdminInventory.jsx`, `StockAdjustmentModal.jsx`

### 3.3. Promotions & Admin Coupons
*   **Endpoints**:
    *   `GET/POST/PUT/DELETE /api/admin/promotions`
    *   `POST /api/admin/promotions/{promotionId}/(activate|pause|expire)`
    *   `GET/POST/DELETE /api/admin/coupons`
    *   `POST /api/admin/coupons/{couponId}/(assign|deactivate)`
*   **Components needed**: `AdminPromotions.jsx`, `AdminCoupons.jsx`

### 3.4. Shipments & Payments
*   **Endpoints**:
    *   `GET/PATCH /api/admin/shipments`
    *   `POST /api/admin/shipments/{shipmentId}/(ready-to-ship|shipped|delivered|delivery-failed|cancelled)`
    *   `GET/POST /api/admin/payments` (refunds, cancellations)
*   **Components needed**: `AdminShipments.jsx`, `AdminPayments.jsx`

### 3.5. User Admin & Review Moderation
*   **Endpoints**:
    *   `GET/POST /api/admin/users`, `/api/admin/users/{userId}/(disable|enable)`
    *   `GET/POST /api/admin/reviews` (approve, reject, hide, restore, delete)
*   **Components needed**: `AdminUsers.jsx`, `AdminReviews.jsx`

### 3.6. System Operations (Search & Notifications)
*   **Endpoints**:
    *   `GET/POST /api/admin/search/documents` & `reindex / rebuild`
    *   `GET/POST/PUT /api/admin/notifications/templates`
*   **Components needed**: `AdminSearchConfig.jsx`, `AdminNotificationTemplates.jsx`
