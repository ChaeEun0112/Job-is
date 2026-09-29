import { useMutation } from '@tanstack/react-query';
import { deleteProfileFile, getProfileFileDownloadUrl } from '@/api/profile';

export default function useProfileDocumentActions() {
  const deleteMutation = useMutation({ mutationFn: deleteProfileFile });
  const downloadMutation = useMutation({ mutationFn: getProfileFileDownloadUrl });

  return {
    deleteProfileFile: deleteMutation.mutateAsync,
    isDeletingProfileFile: deleteMutation.isPending,
    downloadProfileFile: async (fileId: number) => {
      const { downloadUrl } = await downloadMutation.mutateAsync(fileId);
      window.location.assign(downloadUrl);
    },
    isDownloadingProfileFile: downloadMutation.isPending,
  };
}
