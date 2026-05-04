import { useMutation } from '@tanstack/react-query';
import { changePassword } from '../api/authApi';

export const useChangePassword = () => {
  return useMutation({
    mutationFn: changePassword,
  });
};
