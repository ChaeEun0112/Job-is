import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { logout } from '@/api/auth';
import { clearAuth } from '@/store/useAuthStore';

export default function useAccountLogout() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.clear();
      clearAuth();
      navigate('/', { replace: true });
    },
  });

  return {
    logout: logoutMutation.mutate,
    isLoggingOut: logoutMutation.isPending,
    isLogoutError: logoutMutation.isError,
  };
}
