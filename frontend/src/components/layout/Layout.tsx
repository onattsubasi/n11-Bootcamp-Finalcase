import React from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';
import { Footer } from './Footer';

const Layout: React.FC = () => {
  return (
    <div className="min-h-screen bg-background font-sans antialiased selection:bg-primary selection:text-primary-foreground">
      <Navbar />
      <main className="relative z-10">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};

export default Layout;
