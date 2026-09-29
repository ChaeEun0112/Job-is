import { useMutation } from '@tanstack/react-query';
import { postConsent } from '@/api/auth';
import type { ConsentRequest } from '@/api/types/auth.types';

export default function useConsentSubmission() {
  const consentMutation = useMutation({ mutationFn: postConsent });

  return {
    submitConsent: (request: ConsentRequest) => consentMutation.mutateAsync(request),
    isSubmitting: consentMutation.isPending,
  };
}
