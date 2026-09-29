import { useSearchParams } from 'react-router';
import SavedJobsList from '@/features/savedJobs/components/SavedJobsList';

export default function SavedJobsPage() {
  const [searchParams] = useSearchParams();

  return <SavedJobsList isEmptyPreview={searchParams.get('preview') === 'empty'} />;
}
