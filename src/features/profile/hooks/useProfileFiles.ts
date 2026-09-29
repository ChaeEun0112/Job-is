import { useQuery } from '@tanstack/react-query';
import { getProfileFiles } from '@/api/profile';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useProfileFiles() {
  const profileFilesQuery = useQuery({
    queryKey: QUERY_KEYS.PROFILE.FILES(),
    queryFn: getProfileFiles,
  });

  return {
    profileFiles: profileFilesQuery.data ?? [],
    refetchProfileFiles: profileFilesQuery.refetch,
  };
}
