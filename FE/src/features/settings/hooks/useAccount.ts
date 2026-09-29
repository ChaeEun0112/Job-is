import { useQuery } from '@tanstack/react-query';
import { getAccount } from '@/api/auth';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useAccount() {
  return useQuery({
    queryKey: QUERY_KEYS.AUTH.ACCOUNT(),
    queryFn: getAccount,
  });
}
