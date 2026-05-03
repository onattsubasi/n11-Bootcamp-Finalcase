import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useStore } from '../store';

export const RequireAuth = () => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
};

export const RequireRole = ({ allowedRoles }) => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const roles = useStore((state) => state.roles);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!roles.some((role) => allowedRoles.includes(role))) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
