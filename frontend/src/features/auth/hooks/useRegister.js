import { useMutation } from '@tanstack/react-query';
import { registerUser } from '../api/authApi';
import { useStore } from '../../../store';
import { normalizeAuthState } from '../utils/normalizeAuthState';

export const useRegister = () => {
  const setAuth = useStore((state) => state.setAuth);

  return useMutation({
    mutationFn: registerUser,
    onSuccess: (data) => {
      const authState = normalizeAuthState(data);
      if (authState.accessToken) {
        setAuth(authState);
      }
    },
  });
};
