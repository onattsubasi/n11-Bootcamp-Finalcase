import { useMutation } from '@tanstack/react-query';
import { loginUser } from '../api/authApi';
import { useStore } from '@/store';
import { normalizeAuthState } from '../utils/normalizeAuthState';

export const useLogin = () => {
  const setAuth = useStore((state) => state.setAuth);
  
  return useMutation({
    mutationFn: loginUser,
    onSuccess: (data) => {
      setAuth(normalizeAuthState(data));
    },
  });
};
