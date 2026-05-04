import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from '@/components/layout/Layout';
import ProductListPage from '@/pages/ProductListPage';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import ProductDetail from '@/pages/ProductDetail';
import BasketPage from '@/pages/BasketPage';
import SearchResults from '@/pages/SearchResults';
import CheckoutPage from '@/pages/CheckoutPage';
import Orders from '@/pages/Orders';
import OrderDetail from '@/pages/OrderDetail';
import ProfilePage from '@/pages/ProfilePage';
import AdminProducts from '@/pages/AdminProducts';
import AdminOrders from '@/pages/AdminOrders';
import AdminSearch from '@/pages/AdminSearch';
import AdminCheckouts from '@/pages/AdminCheckouts';
import AdminPayments from '@/pages/AdminPayments';
import AdminUsers from '@/pages/AdminUsers';
import AdminPromotions from '@/pages/AdminPromotions';
import AdminBrands from '@/pages/AdminBrands';
import AdminCategories from '@/pages/AdminCategories';
import AdminInventory from '@/pages/AdminInventory';
import AdminNotifications from '@/pages/AdminNotifications';
import AdminReviews from '@/pages/AdminReviews';
import AdminShipments from '@/pages/AdminShipments';
import CustomerShipments from '@/pages/CustomerShipments';
import CustomerNotifications from '@/pages/CustomerNotifications';
import { RequireAuth, RequireRole } from './guards';

export const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<Layout />}>
          <Route index element={<ProductListPage />} />
          <Route path='login' element={<Login />} />
          <Route path='register' element={<Register />} />
          <Route path='product/:id' element={<ProductDetail />} />
          <Route path='search' element={<SearchResults />} />
          <Route element={<RequireAuth />}>
            <Route path='basket' element={<BasketPage />} />
            <Route path='checkout' element={<CheckoutPage />} />
            <Route path='orders' element={<Orders />} />
            <Route path='orders/:id' element={<OrderDetail />} />
            <Route path='shipments' element={<CustomerShipments />} />
            <Route path='notifications' element={<CustomerNotifications />} />
            <Route path='profile' element={<ProfilePage />} />
          </Route>
          <Route element={<RequireRole allowedRoles={['ADMIN']} />}>
            <Route path='admin/products' element={<AdminProducts />} />
            <Route path='admin/brands' element={<AdminBrands />} />
            <Route path='admin/categories' element={<AdminCategories />} />
            <Route path='admin/inventory' element={<AdminInventory />} />
            <Route path='admin/orders' element={<AdminOrders />} />
            <Route path='admin/shipments' element={<AdminShipments />} />
            <Route path='admin/checkouts' element={<AdminCheckouts />} />
            <Route path='admin/payments' element={<AdminPayments />} />
            <Route path='admin/users' element={<AdminUsers />} />
            <Route path='admin/promotions' element={<AdminPromotions />} />
            <Route path='admin/search' element={<AdminSearch />} />
            <Route path='admin/notifications' element={<AdminNotifications />} />
            <Route path='admin/reviews' element={<AdminReviews />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
};
