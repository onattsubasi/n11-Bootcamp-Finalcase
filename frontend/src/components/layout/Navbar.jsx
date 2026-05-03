import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useBasketQuery } from '../../features/basket/hooks/useBasket';
import { useLogout } from '../../features/auth/hooks/useLogout';
import { useStore } from '../../store';
import { NotificationBell } from '../../features/notifications/components/NotificationBell';
import { useAutocomplete } from '../../features/search/hooks/useSearch';
import { useDebounce } from '../../lib/hooks/useDebounce';

export const Navbar = () => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const roles = useStore((state) => state.roles);
  const { mutate: logout, isPending: isLoggingOut } = useLogout();
  const { data: basket } = useBasketQuery(isAuthenticated);
  const itemCount = basket?.items?.reduce((acc, item) => acc + item.quantity, 0) || 0;
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const debouncedQuery = useDebounce(searchQuery, 300);
  const { data: suggestions } = useAutocomplete(debouncedQuery);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate('/search?q=' + searchQuery.trim());
      setShowSuggestions(false);
    }
  };

  return (
    <nav className='flex items-center justify-between p-4 bg-gray-800 text-white'>
      <Link to='/' className='text-xl font-bold'>Logo</Link>
      <div className='flex gap-4 items-center flex-1 max-w-xl mx-4'>
        <form onSubmit={handleSearch} className="relative flex-1">
          <input 
            name='q'
            type='text' 
            placeholder='Search products...' 
            className='w-full px-3 py-1.5 text-black rounded-lg outline-none focus:ring-2 focus:ring-blue-500' 
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setShowSuggestions(true);
            }}
            onFocus={() => setShowSuggestions(true)}
            onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
          />
          {showSuggestions && suggestions?.length > 0 && (
            <div className="absolute left-0 right-0 mt-1 bg-white text-gray-900 rounded-lg shadow-xl z-[60] border overflow-hidden">
              {suggestions.map((item, idx) => (
                <button
                  key={idx}
                  type="button"
                  className="w-full text-left px-4 py-2 hover:bg-gray-100 text-sm flex items-center justify-between"
                  onClick={() => {
                    setSearchQuery(item.text);
                    navigate('/search?q=' + item.text);
                    setShowSuggestions(false);
                  }}
                >
                  <span>{item.text}</span>
                  {item.category && <span className="text-[10px] bg-gray-100 px-1.5 py-0.5 rounded text-gray-500">{item.category}</span>}
                </button>
              ))}
            </div>
          )}
        </form>
      </div>
      <div className='flex gap-4 items-center'>
        {isAuthenticated ? (
          <>
            <NotificationBell />
            <Link to='/orders' className='hover:underline'>Orders</Link>
            <Link to='/shipments' className='hover:underline'>Shipments</Link>
            <Link to='/profile' className='hover:underline'>Profile</Link>
            {roles.includes('ADMIN') ? (
              <div className="relative group">
                <button className="hover:underline flex items-center gap-1">Admin ▼</button>
                <div className="absolute right-0 mt-2 w-48 bg-white text-gray-800 rounded shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50 flex flex-col py-2">
                  <Link to='/admin/products' className='px-4 py-2 hover:bg-gray-100'>Products</Link>
                  <Link to='/admin/brands' className='px-4 py-2 hover:bg-gray-100'>Brands</Link>
                  <Link to='/admin/categories' className='px-4 py-2 hover:bg-gray-100'>Categories</Link>
                  <Link to='/admin/inventory' className='px-4 py-2 hover:bg-gray-100'>Inventory</Link>
                  <Link to='/admin/orders' className='px-4 py-2 hover:bg-gray-100'>Orders</Link>
                  <Link to='/admin/shipments' className='px-4 py-2 hover:bg-gray-100'>Shipments</Link>
                  <Link to='/admin/checkouts' className='px-4 py-2 hover:bg-gray-100'>Checkouts</Link>
                  <Link to='/admin/payments' className='px-4 py-2 hover:bg-gray-100'>Payments</Link>
                  <Link to='/admin/promotions' className='px-4 py-2 hover:bg-gray-100'>Promotions</Link>
                  <Link to='/admin/search' className='px-4 py-2 hover:bg-gray-100'>Search</Link>
                  <Link to='/admin/users' className='px-4 py-2 hover:bg-gray-100'>Users</Link>
                  <Link to='/admin/notifications' className='px-4 py-2 hover:bg-gray-100'>Notifications</Link>
                  <Link to='/admin/reviews' className='px-4 py-2 hover:bg-gray-100'>Reviews</Link>
                </div>
              </div>
            ) : null}
            <Link to='/basket' className='hover:underline font-bold'>
              Basket ({itemCount})
            </Link>
            <button
              type='button'
              onClick={() => logout()}
              disabled={isLoggingOut}
              className='hover:underline disabled:opacity-60'
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to='/login' className='hover:underline'>Login</Link>
            <Link to='/register' className='hover:underline'>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};
