import { create } from 'zustand';

export type RecommendationLetterStatus = 'saved' | 'dismissed' | 'unprocessed';

interface RecommendationDeckState {
  statusByLetterId: Record<string, RecommendationLetterStatus>;
  setStatus: (letterId: string, status: RecommendationLetterStatus) => void;
  viewedLetterIds: Record<string, true>;
  markViewed: (letterId: string) => void;
}

export const useRecommendationDeckStore = create<RecommendationDeckState>((set) => ({
  statusByLetterId: {},
  setStatus: (letterId, status) =>
    set((state) => ({
      statusByLetterId: { ...state.statusByLetterId, [letterId]: status },
    })),
  viewedLetterIds: {},
  markViewed: (letterId) =>
    set((state) =>
      state.viewedLetterIds[letterId]
        ? state
        : { viewedLetterIds: { ...state.viewedLetterIds, [letterId]: true } },
    ),
}));

export function getRecommendationLetterStatus(
  statusByLetterId: Record<string, RecommendationLetterStatus>,
  letterId: string,
  fallback: RecommendationLetterStatus = 'unprocessed',
): RecommendationLetterStatus {
  return statusByLetterId[letterId] ?? fallback;
}
