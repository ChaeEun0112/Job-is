import { useQuery } from '@tanstack/react-query';
import { getTodayBriefing, getTodayBriefingStatus } from '@/api/recommendations';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useTodayBriefing() {
  return useQuery({
    queryKey: QUERY_KEYS.RECOMMENDATIONS.BRIEFING(),
    queryFn: getTodayBriefing,
  });
}

export function useTodayBriefingCards() {
  return useQuery({
    queryKey: QUERY_KEYS.RECOMMENDATIONS.BRIEFING_STATUS(),
    queryFn: getTodayBriefingStatus,
  });
}
