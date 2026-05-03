import { useMutation } from '@tanstack/react-query';
import { logoutAll } from '../api/authApi';
import { useStore } from '../../../store';
import { useNavigate } from 'react-router-dom';

export const useLogoutAll = () => {
  const clearAuth = useStore((state) => state.clearAuth);
  const navigate = useNavigate();

  return useMutation({
    mutationFn: logoutAll,
    onSuccess: () => {
      clearAuth();
      navigate('/login');
    },
  });
};
