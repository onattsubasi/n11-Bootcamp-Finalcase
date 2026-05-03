import { useState } from 'react';
import { useProfile } from '../features/profile/hooks/useProfile';
import ProfileForm from '../features/profile/components/ProfileForm';
import AddressBook from '../features/profile/components/AddressBook';
import ChangePasswordForm from '../features/profile/components/ChangePasswordForm';
import FavoritesTab from '../features/profile/components/FavoritesTab';
import CouponsTab from '../features/profile/components/CouponsTab';
import ProductListsTab from '../features/profile/components/ProductListsTab';
import { Spinner } from '../components/ui/Spinner';

const ProfilePage = () => {
  const { data, isPending, isError, error } = useProfile();
  const [activeTab, setActiveTab] = useState('profile');

  if (isPending) return <div className="p-8 max-w-6xl mx-auto flex justify-center"><Spinner size="lg" /></div>;
  if (isError) return <div className="p-8 max-w-6xl mx-auto text-red-500">Failed to load profile: {error.message}</div>;

  const tabs = [
    { id: 'profile', label: 'Profile Information' },
    { id: 'addresses', label: 'Address Book' },
    { id: 'favorites', label: 'Favorites' },
    { id: 'lists', label: 'My Lists' },
    { id: 'coupons', label: 'My Coupons' },
    { id: 'security', label: 'Security' },
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return <ProfileForm profile={data} />;
      case 'addresses':
        return <AddressBook />;
      case 'favorites':
        return <FavoritesTab />;
      case 'lists':
        return <ProductListsTab />;
      case 'coupons':
        return <CouponsTab />;
      case 'security':
        return <ChangePasswordForm />;
      default:
        return null;
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-4 sm:p-6 lg:p-8 space-y-8">
      <header className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Account Settings</h1>
        <p className="mt-2 text-gray-600">Manage your profile, addresses, and account preferences.</p>
      </header>

      <div className="flex flex-col lg:flex-row gap-8">
        {/* Navigation Tabs */}
        <aside className="w-full lg:w-64 flex-shrink-0">
          <nav className="flex flex-row lg:flex-column overflow-x-auto lg:overflow-x-visible space-x-1 lg:space-x-0 lg:space-y-1 bg-gray-100 p-1 rounded-lg">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`whitespace-nowrap px-4 py-2 text-sm font-medium rounded-md transition-all ${
                  activeTab === tab.id
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200'
                } w-full text-left`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </aside>

        {/* Tab Content */}
        <main className="flex-1 min-w-0">
          <div className="animate-in fade-in slide-in-from-bottom-4 duration-300">
            {renderTabContent()}
          </div>
        </main>
      </div>
    </div>
  );
};

export default ProfilePage;
