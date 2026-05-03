# Marketplace Frontend

React single-page application for a marketplace experience. The app is built with Vite, React Router, TanStack Query, Zustand, Axios, and Tailwind CSS.

## Overview

This frontend connects to a backend API and provides:

- Product browsing and search
- Product detail pages
- Basket and checkout flows
- Order history
- Customer profile and address management
- Admin product and order management
- Authentication with login, registration, token refresh, and protected routes

Server state is handled with TanStack Query, while client state such as auth and UI flags is stored in Zustand.

## Tech Stack

- React 19
- Vite
- React Router DOM
- TanStack Query
- Zustand
- Axios
- Tailwind CSS
- react-hot-toast
- lucide-react

## Project Structure

- `src/App.jsx` - app shell with query client and toast provider
- `src/router/` - route definitions and auth guards
- `src/pages/` - page-level views
- `src/features/` - feature-specific API hooks, utilities, and components
- `src/store/` - Zustand auth and UI state
- `src/api/` - shared Axios client and route constants
- `src/lib/queryClient.js` - shared TanStack Query client

## Features

Public and customer routes currently include:

- Home product listing
- Product details
- Login and registration
- Basket
- Checkout
- Orders
- Profile
- Search results

Admin routes currently include:

- Admin products
- Admin orders

## Backend Connection

The API client uses the following base URL configuration:

- `VITE_API_BASE_URL` if set
- `http://localhost:8080` as the default fallback

Requests are sent with credentials enabled, and authenticated calls attach the bearer access token from Zustand. If the backend returns a `401`, the client attempts token refresh before redirecting to `/login`.

## Getting Started

### Prerequisites

- Node.js 18 or newer
- npm

### Install

```bash
npm install
```

### Run locally

```bash
npm run dev
```

### Build for production

```bash
npm run build
```

### Preview the production build

```bash
npm run preview
```

## Environment Variables

Create a `.env` file if you want to override the backend URL:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Notes

- Legacy auth storage keys are cleared on startup to avoid stale sessions.
- Remaining backend alignment work is tracked in `remaining_requirements.md`.