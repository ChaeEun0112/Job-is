import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

interface AuthState {
  accessToken: string | null;
  userId: number | null;
  onboardingCompleted: boolean | null;
}

interface AuthActions {
  setAccessToken: (accessToken: string | null) => void;
  setUserId: (userId: number | null) => void;
  setOnboardingCompleted: (onboardingCompleted: boolean | null) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    (set) => ({
      accessToken: null,
      userId: null,
      onboardingCompleted: null,
      setAccessToken: (accessToken) => set({ accessToken }),
      setUserId: (userId) => set({ userId }),
      setOnboardingCompleted: (onboardingCompleted) => set({ onboardingCompleted }),
      clearAuth: () => set({ accessToken: null, userId: null, onboardingCompleted: null }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => sessionStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        userId: state.userId,
        onboardingCompleted: state.onboardingCompleted,
      }),
    },
  ),
);

export const getAccessToken = () => useAuthStore.getState().accessToken;
export const setAccessToken = (accessToken: string | null) =>
  useAuthStore.getState().setAccessToken(accessToken);
export const setUserId = (userId: number | null) => useAuthStore.getState().setUserId(userId);
export const setOnboardingCompleted = (onboardingCompleted: boolean | null) =>
  useAuthStore.getState().setOnboardingCompleted(onboardingCompleted);
export const clearAuth = () => useAuthStore.getState().clearAuth();

export const getPostLoginPath = (onboardingCompleted: boolean | null) =>
  onboardingCompleted ? '/recommendations' : '/onboarding';
