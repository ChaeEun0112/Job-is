import { useQuery } from '@tanstack/react-query';
import { getContent, getContents } from '@/api/recommendations';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useContents() {
  return useQuery({
    queryKey: QUERY_KEYS.RECOMMENDATIONS.CONTENTS(),
    queryFn: getContents,
  });
}

export function useContentDetail(contentId: number) {
  return useQuery({
    queryKey: QUERY_KEYS.RECOMMENDATIONS.CONTENT_DETAIL(contentId),
    queryFn: () => getContent(contentId),
    enabled: Number.isFinite(contentId),
  });
}
