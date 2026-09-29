import useAuthSession from '@/features/login/hooks/useAuthSession';

export default function AuthSessionSync() {
  useAuthSession();
  return null;
}
