import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

const Layout = () => {
  return (
    <div className='min-h-screen bg-gray-50 flex flex-col'>
      <Navbar />
      <main className='flex-1 p-4'>
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;
