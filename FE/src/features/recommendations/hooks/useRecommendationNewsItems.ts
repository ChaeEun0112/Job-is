import { useMemo } from 'react';
import { useContents } from './useContents';
import { mapContentSummary } from '../mapContent';

export default function useRecommendationNewsItems() {
  const contentsQuery = useContents();
  const newsItems = useMemo(
    () => (contentsQuery.data ?? []).map(mapContentSummary),
    [contentsQuery.data],
  );

  return { newsItems, isLoading: contentsQuery.isLoading };
}
