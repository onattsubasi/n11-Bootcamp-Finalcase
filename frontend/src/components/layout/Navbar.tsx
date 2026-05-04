import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import {
  Search as SearchIcon,
  ShoppingBag,
  User,
  Bell,
  LogOut,
  ChevronDown,
  Menu,
  ChevronRight,
  X,
  Shield,
  Zap,
  Activity,
  Layers,
} from "lucide-react";
import { useBasketQuery } from "../../features/basket/hooks/useBasket";
import { useLogout } from "../../features/auth/hooks/useLogout";
import { useStore } from "../../store";
import { NotificationBell } from "../../features/notifications/components/NotificationBell";
import { useAutocomplete } from "../../features/search/hooks/useSearch";
import { useDebounce } from "../../lib/hooks/useDebounce";
import { cn } from "@/lib/utils/cn";

export const Navbar: React.FC = () => {
  const location = useLocation();
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const roles = useStore((state) => state.roles);
  const { mutate: logout, isPending: isLoggingOut } = useLogout();
  const { data: basket } = useBasketQuery(isAuthenticated);
  const itemCount =
    basket?.items?.reduce((acc: number, item: any) => acc + item.quantity, 0) ||
    0;
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState("");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const debouncedQuery = useDebounce(searchQuery, 300);
  const { data: suggestions } = useAutocomplete(debouncedQuery);
  const [showSuggestions, setShowSuggestions] = useState(false);

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate("/search?q=" + encodeURIComponent(searchQuery.trim()));
      setShowSuggestions(false);
      setIsMobileMenuOpen(false);
    }
  };

  return (
    <nav
      className={cn(
        "sticky top-0 z-[100] w-full transition-all duration-500",
        isScrolled
          ? "bg-gray-950/95 backdrop-blur-xl border-b border-white/5 py-2 shadow-2xl"
          : "bg-gray-950/80 backdrop-blur-md border-b border-transparent py-4",
      )}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 sm:h-20 gap-8">
          {/* Brand Identity */}
          <Link
            to="/"
            className="flex items-center gap-4 group transition-transform active:scale-95 shrink-0"
          >
            <div className="relative">
              <div className="absolute inset-0 bg-primary/20 blur-xl rounded-full scale-150 opacity-0 group-hover:opacity-100 transition-opacity duration-700" />
              <div className="relative bg-primary p-2.5 rounded-2xl shadow-2xl shadow-primary/40 group-hover:rotate-12 transition-all duration-500">
                <ShoppingBag className="w-5 h-5 sm:w-6 sm:h-6 text-primary-foreground" />
              </div>
            </div>
            <div className="flex flex-col">
              <span className="text-xl sm:text-2xl font-black tracking-tighter uppercase leading-none text-white">
                FINALCASE
              </span>
              <span className="text-[8px] font-black tracking-[0.4em] text-primary uppercase opacity-60">
                E-Commerce Platform
              </span>
            </div>
          </Link>

          {/* Neural Search Engine - Desktop */}
          <div className="hidden md:block flex-1 max-w-xl relative">
            <form onSubmit={handleSearch} className="relative group">
              <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                <SearchIcon className="h-4 w-4 text-white/20 group-focus-within:text-primary transition-colors" />
              </div>
              <input
                name="q"
                type="text"
                placeholder="Search neural catalog..."
                className="w-full bg-white/5 border border-white/5 text-white rounded-[1.25rem] py-3.5 pl-14 pr-4 outline-none focus:bg-white/10 focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all placeholder:text-white/20 font-bold text-xs uppercase tracking-widest"
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setShowSuggestions(true);
                }}
                onFocus={() => setShowSuggestions(true)}
                onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
              />

              {/* Autocomplete Interface */}
              {showSuggestions && suggestions && suggestions.length > 0 && (
                <div className="absolute left-0 right-0 mt-4 bg-gray-900/95 backdrop-blur-2xl border border-white/5 rounded-[2rem] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.8)] z-[110] overflow-hidden animate-in fade-in slide-in-from-top-4 duration-500">
                  <div className="p-3">
                    <div className="px-4 py-3 text-[8px] font-black text-white/20 uppercase tracking-[0.4em] border-b border-white/5 mb-2">
                      Neural Suggestions
                    </div>
                    {suggestions.map((item: any, idx: number) => (
                      <button
                        key={idx}
                        type="button"
                        className="w-full text-left px-5 py-4 hover:bg-white/5 rounded-[1.25rem] text-[10px] font-black uppercase tracking-widest flex items-center justify-between transition-all group/item"
                        onClick={() => {
                          setSearchQuery(item.text);
                          navigate(
                            "/search?q=" + encodeURIComponent(item.text),
                          );
                          setShowSuggestions(false);
                        }}
                      >
                        <div className="flex items-center gap-3">
                          <Activity
                            size={14}
                            className="text-primary/40 group-hover/item:text-primary"
                          />
                          <span className="text-white/60 group-hover/item:text-white">
                            {item.text}
                          </span>
                        </div>
                        {item.category ? (
                          <span className="text-[9px] font-black bg-primary/10 px-2 py-0.5 rounded-lg text-primary group-hover/item:bg-primary group-hover/item:text-primary-foreground transition-all">
                            {item.category}
                          </span>
                        ) : (
                          <ChevronRight
                            size={14}
                            className="text-white/10 group-hover/item:text-primary translate-x-0 group-hover/item:translate-x-1 transition-all"
                          />
                        )}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </form>
          </div>

          {/* Tactical Hub */}
          <div className="hidden md:flex items-center gap-6">
            {isAuthenticated ? (
              <>
                <div className="flex items-center gap-1.5 bg-white/5 p-1.5 rounded-[1.5rem] border border-white/5 shadow-inner">
                  <NotificationBell />

                  <Link
                    to="/profile"
                    className={cn(
                      "p-2.5 rounded-xl transition-all hover:bg-white/5 group relative",
                      location.pathname === "/profile"
                        ? "text-primary"
                        : "text-white/40 hover:text-white",
                    )}
                  >
                    <User className="w-5 h-5" />
                    {location.pathname === "/profile" && (
                      <div className="absolute -bottom-1 left-1/2 -translate-x-1/2 w-1 h-1 bg-primary rounded-full" />
                    )}
                  </Link>

                  <Link
                    to="/basket"
                    className={cn(
                      "p-2.5 rounded-xl transition-all hover:bg-white/5 group relative",
                      location.pathname === "/basket"
                        ? "text-primary"
                        : "text-white/40 hover:text-white",
                    )}
                  >
                    <ShoppingBag className="w-5 h-5" />
                    {itemCount > 0 ? (
                      <span className="absolute top-1 right-1 bg-primary text-primary-foreground text-[8px] font-black rounded-full min-w-[16px] h-4 flex items-center justify-center border-2 border-gray-950 group-hover:scale-110 transition-transform">
                        {itemCount}
                      </span>
                    ) : null}
                  </Link>
                </div>

                <div className="h-8 w-px bg-white/5" />

                {roles.includes("ADMIN") && (
                  <div className="relative group">
                    <button className="flex items-center gap-3 bg-primary/10 text-primary px-5 py-3 rounded-2xl border border-primary/20 hover:bg-primary/20 transition-all font-black uppercase tracking-widest text-[10px] shadow-lg shadow-primary/5">
                      <Shield size={14} />
                      <span>Admin Hub</span>
                      <ChevronDown className="w-4 h-4 opacity-40 group-hover:rotate-180 transition-transform duration-500" />
                    </button>

                    <div className="absolute right-0 mt-4 w-72 bg-gray-900/95 backdrop-blur-2xl border border-white/5 rounded-[2.5rem] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.8)] opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-500 z-[120] p-4">
                      <div className="space-y-1">
                        <div className="px-5 py-3 text-[8px] font-black text-white/20 uppercase tracking-[0.4em] mb-2 flex items-center gap-2">
                          <Layers size={12} /> Commercial Ops
                        </div>
                        <AdminLink
                          to="/admin/products"
                          label="Inventory Core"
                          icon={<Zap size={14} />}
                        />
                        <AdminLink
                          to="/admin/orders"
                          label="Global Logistics"
                          icon={<Activity size={14} />}
                        />
                        <div className="h-px bg-white/5 my-3 mx-2" />
                        <div className="px-5 py-3 text-[8px] font-black text-white/20 uppercase tracking-[0.4em] mb-2">
                          Protocol Control
                        </div>
                        <AdminLink
                          to="/admin/notifications"
                          label="Neural Dispatch"
                        />
                      </div>
                    </div>
                  </div>
                )}

                <button
                  type="button"
                  onClick={() => logout()}
                  disabled={isLoggingOut}
                  className="w-12 h-12 bg-white/5 border border-white/5 hover:bg-destructive/10 hover:text-destructive hover:border-destructive/20 rounded-2xl transition-all flex items-center justify-center disabled:opacity-50 group"
                  title="Terminate Session"
                >
                  <LogOut className="w-5 h-5 transition-transform group-hover:translate-x-0.5" />
                </button>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <Link
                  to="/login"
                  className="px-6 py-3 text-[10px] font-black text-white/40 hover:text-white transition-colors uppercase tracking-[0.2em]"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="bg-primary text-primary-foreground px-8 py-3.5 rounded-2xl text-[10px] font-black hover:bg-primary/90 hover:scale-105 active:scale-95 transition-all shadow-2xl shadow-primary/20 uppercase tracking-[0.2em]"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>

          {/* Interaction Toggle - Mobile */}
          <button
            className="md:hidden w-12 h-12 rounded-2xl bg-white/5 flex items-center justify-center text-white/60 hover:text-white transition-all active:scale-90"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Neural Interface Expansion - Mobile */}
      <div
        className={cn(
          "md:hidden overflow-hidden transition-all duration-700 bg-gray-950 border-t border-white/5",
          isMobileMenuOpen ? "max-h-[90vh] pb-10" : "max-h-0",
        )}
      >
        <div className="px-6 pt-8 space-y-10">
          <form onSubmit={handleSearch} className="relative">
            <SearchIcon className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-white/20" />
            <input
              type="text"
              placeholder="Neural catalog search..."
              className="w-full bg-white/5 border border-white/5 rounded-2xl py-4 pl-12 pr-4 text-[10px] font-black uppercase tracking-widest outline-none focus:ring-1 focus:ring-primary/40"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </form>

          <div className="space-y-4">
            {isAuthenticated ? (
              <>
                <div className="grid grid-cols-2 gap-4">
                  <MobileNavItem
                    to="/profile"
                    icon={<User className="w-5 h-5" />}
                    label="Identity"
                    onClick={() => setIsMobileMenuOpen(false)}
                  />
                  <MobileNavItem
                    to="/basket"
                    icon={<ShoppingBag className="w-5 h-5" />}
                    label="Bag"
                    badge={itemCount}
                    onClick={() => setIsMobileMenuOpen(false)}
                  />
                </div>
                <MobileNavItem
                  to="/orders"
                  icon={<ChevronRight className="w-5 h-5" />}
                  label="Orders"
                  onClick={() => setIsMobileMenuOpen(false)}
                />

                {roles.includes("ADMIN") && (
                  <div className="bg-primary/5 rounded-[2rem] p-4 border border-primary/10">
                    <div className="px-4 py-2 text-[8px] font-black text-primary uppercase tracking-[0.4em] mb-2">
                      Admin Core
                    </div>
                    <div className="space-y-1">
                      <AdminLink
                        to="/admin/products"
                        label="Products"
                        onClick={() => setIsMobileMenuOpen(false)}
                      />
                      <AdminLink
                        to="/admin/orders"
                        label="Global Sales"
                        onClick={() => setIsMobileMenuOpen(false)}
                      />
                    </div>
                  </div>
                )}

                <button
                  onClick={() => {
                    logout();
                    setIsMobileMenuOpen(false);
                  }}
                  className="flex items-center justify-center gap-3 w-full p-5 rounded-2xl text-[10px] font-black uppercase tracking-[0.3em] text-destructive bg-destructive/10 border border-destructive/20"
                >
                  <LogOut size={16} /> Terminate Session
                </button>
              </>
            ) : (
              <div className="flex flex-col gap-4 pt-4">
                <Link
                  to="/login"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="w-full py-5 text-center rounded-2xl border border-white/10 text-[10px] font-black uppercase tracking-widest"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="w-full py-5 text-center rounded-2xl bg-primary text-primary-foreground text-[10px] font-black uppercase tracking-widest shadow-2xl shadow-primary/20"
                >
                  Initialize Identity
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

const MobileNavItem: React.FC<{
  to: string;
  icon: React.ReactNode;
  label: string;
  badge?: number;
  onClick: () => void;
}> = ({ to, icon, label, badge, onClick }) => (
  <Link
    to={to}
    onClick={onClick}
    className="flex items-center justify-between w-full p-5 rounded-[1.5rem] bg-white/5 border border-white/5 hover:bg-white/10 transition-all group"
  >
    <div className="flex items-center gap-4">
      <span className="text-primary">{icon}</span>
      <span className="text-[10px] font-black uppercase tracking-widest text-white/60 group-hover:text-white">
        {label}
      </span>
    </div>
    {badge && (
      <span className="bg-primary text-primary-foreground text-[9px] font-black px-2.5 py-1 rounded-full">
        {badge}
      </span>
    )}
  </Link>
);

const AdminLink: React.FC<{
  to: string;
  label: string;
  icon?: React.ReactNode;
  onClick?: () => void;
}> = ({ to, label, icon, onClick }) => (
  <Link
    to={to}
    onClick={onClick}
    className="px-5 py-4 hover:bg-white/10 rounded-2xl text-[10px] font-black uppercase tracking-widest text-white/40 hover:text-white transition-all flex items-center justify-between group/alink"
  >
    <div className="flex items-center gap-3">
      {icon && (
        <span className="text-primary/40 group-hover/alink:text-primary transition-colors">
          {icon}
        </span>
      )}
      {label}
    </div>
    <ChevronRight
      size={14}
      className="opacity-0 group-hover/alink:opacity-100 transition-opacity"
    />
  </Link>
);
