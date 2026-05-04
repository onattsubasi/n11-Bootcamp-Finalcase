import React, { useState } from 'react';
import { 
  User, 
  MapPin, 
  Heart, 
  List, 
  Tag, 
  Shield, 
  ChevronRight,
  LogOut,
  Camera,
  Settings
} from 'lucide-react';
import { useProfile, type UserProfile } from '@/features/profile/hooks/useProfile';
import { ProfileForm } from '@/features/profile/components/ProfileForm';
import { AddressBook } from '@/features/profile/components/AddressBook';
import ChangePasswordForm from '@/features/profile/components/ChangePasswordForm';
import FavoritesTab from '@/features/profile/components/FavoritesTab';
import CouponsTab from '@/features/profile/components/CouponsTab';
import ProductListsTab from '@/features/profile/components/ProductListsTab';
import { Spinner } from '@/components/ui/Spinner';
import { Button } from '@/components/ui/Button';
import { useStore } from '@/store';

const ProfilePage: React.FC = () => {
  const { data: profileData, isPending, isError, error } = useProfile();
  const profile = profileData as UserProfile | undefined;
  const [activeTab, setActiveTab] = useState('profile');
  const clearAuth = useStore(state => state.clearAuth);
  const handleLogout = () => {
    clearAuth();
  };

  if (isPending) return (
    <div className="flex flex-col min-h-[60vh] items-center justify-center gap-4">
      <Spinner size="lg" />
      <span className="text-[10px] font-black uppercase tracking-[0.4em] text-muted-foreground animate-pulse">Synchronizing Cloud Identity...</span>
    </div>
  );

  if (isError) return (
    <div className="max-w-xl mx-auto py-24 text-center px-6 bg-card border border-destructive/20 rounded-[3rem] shadow-xl mt-12">
      <h2 className="text-2xl font-black text-foreground uppercase tracking-tight">Identity Access Failure</h2>
      <p className="text-muted-foreground font-medium mt-2 mb-8">{(error as any).message || 'Failed to authenticate session.'}</p>
      <Button onClick={() => globalThis.location.reload()} variant="primary" className="rounded-2xl px-10 h-14 font-black uppercase tracking-widest text-xs">Retry Connection</Button>
    </div>
  );

  const tabs = [
    { id: 'profile', label: 'Identity', icon: User, description: 'Personal details & bio' },
    { id: 'addresses', label: 'Logistics', icon: MapPin, description: 'Shipping & billing targets' },
    { id: 'favorites', label: 'Favorites', icon: Heart, description: 'Saved premium items' },
    { id: 'lists', label: 'Collections', icon: List, description: 'Custom curated lists' },
    { id: 'coupons', label: 'Privileges', icon: Tag, description: 'Exclusive offers & codes' },
    { id: 'security', label: 'Security', icon: Shield, description: 'Access & protection' },
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return <ProfileForm />;
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
    <div className="mx-auto max-w-7xl px-4 py-16">
      <div className="grid lg:grid-cols-12 gap-16 items-start">
        
        {/* Sidebar Navigation */}
        <aside className="lg:col-span-4 space-y-8 lg:sticky lg:top-24">
           {/* Profile Card Summary */}
           <div className="bg-gray-950 text-white rounded-[3rem] p-10 shadow-2xl relative overflow-hidden border border-white/5 group">
              <div className="absolute top-0 right-0 w-64 h-64 bg-primary/10 rounded-full blur-[80px] -mr-32 -mt-32 transition-transform duration-1000 group-hover:scale-110" />
              
              <div className="relative z-10 space-y-8 text-center sm:text-left">
                 <div className="flex flex-col sm:flex-row items-center gap-8">
                    <div className="relative">
                       <div className="w-24 h-24 bg-primary rounded-[2rem] flex items-center justify-center font-black text-4xl text-primary-foreground shadow-2xl shadow-primary/40 group-hover:rotate-6 transition-all duration-500">
                          {profile?.firstName?.[0] || 'U'}
                       </div>
                       <button className="absolute -bottom-2 -right-2 bg-white text-gray-900 p-2.5 rounded-2xl shadow-xl hover:scale-110 active:scale-95 transition-all">
                          <Camera size={16} />
                       </button>
                    </div>
                    <div className="space-y-1">
                       <h2 className="text-3xl font-black tracking-tighter uppercase leading-none">{profile?.firstName} {profile?.lastName}</h2>
                       <p className="text-[10px] font-black uppercase tracking-[0.2em] text-white/40">{profile?.email || 'Authenticated User'}</p>
                       <div className="pt-2 flex items-center justify-center sm:justify-start gap-2">
                          <span className="bg-emerald-500/20 text-emerald-400 text-[8px] font-black px-2 py-0.5 rounded-full border border-emerald-500/20 uppercase tracking-widest">Premium Member</span>
                       </div>
                    </div>
                 </div>
                 
                 <div className="h-px bg-white/5 w-full" />
                 
                 <nav className="space-y-2">
                    {tabs.map((tab) => (
                      <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id)}
                        className={`w-full flex items-center justify-between p-4 rounded-[1.5rem] transition-all group/btn ${
                          activeTab === tab.id
                            ? 'bg-white text-gray-900 shadow-xl scale-[1.02]'
                            : 'text-white/40 hover:text-white hover:bg-white/5'
                        }`}
                      >
                        <div className="flex items-center gap-4">
                           <tab.icon size={20} className={activeTab === tab.id ? 'text-primary' : 'text-inherit'} />
                           <div className="text-left">
                              <p className="text-[10px] font-black uppercase tracking-widest leading-none">{tab.label}</p>
                              <p className={`text-[9px] font-medium mt-1 ${activeTab === tab.id ? 'text-gray-500' : 'text-white/20'}`}>{tab.description}</p>
                           </div>
                        </div>
                        <ChevronRight size={14} className={`transition-transform duration-300 ${activeTab === tab.id ? 'translate-x-0' : '-translate-x-2 opacity-0'}`} />
                      </button>
                    ))}
                 </nav>

                 <div className="pt-4">
                    <button 
                      onClick={handleLogout}
                      className="w-full flex items-center justify-center gap-3 p-5 rounded-[1.5rem] bg-red-500/10 text-red-500 font-black text-[10px] uppercase tracking-widest hover:bg-red-500 hover:text-white transition-all shadow-xl shadow-red-500/5"
                    >
                       <LogOut size={16} />
                       <span>Terminate Session</span>
                    </button>
                 </div>
              </div>
           </div>
        </aside>

        {/* Main Content Area */}
        <main className="lg:col-span-8 space-y-12 min-w-0">
           <header className="flex flex-col md:flex-row md:items-end justify-between gap-8 pb-12 border-b border-border">
              <div className="space-y-4">
                 <div className="flex items-center gap-3 text-primary font-bold text-[10px] uppercase tracking-[0.4em]">
                    <Settings size={14} />
                    <span>Control Panel</span>
                 </div>
                 <h1 className="text-6xl font-black text-foreground tracking-tighter uppercase leading-none">
                    {tabs.find(t => t.id === activeTab)?.label}
                 </h1>
              </div>
              <div className="flex items-center gap-6">
                 <div className="text-right hidden sm:block">
                    <p className="text-[10px] font-black uppercase tracking-widest opacity-40">Account ID</p>
                    <p className="text-xs font-mono font-bold text-foreground">UID-{(profile?.id ?? 'unknown').substring(0, 8).toUpperCase()}</p>
                 </div>
                 <div className="h-10 w-px bg-border" />
                 <div className="w-12 h-12 bg-muted rounded-2xl flex items-center justify-center text-muted-foreground border border-border">
                    <Shield size={20} />
                 </div>
              </div>
           </header>

           <div className="animate-in fade-in slide-in-from-bottom-8 duration-700 ease-out">
              {renderTabContent()}
           </div>
        </main>

      </div>
    </div>
  );
};

export default ProfilePage;
