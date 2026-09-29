import { useNavigate } from 'react-router';
import ProfileSettingsForm from '@/features/profile/components/ProfileSettingsForm';

export default function ProfilePage() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-1 items-start justify-center bg-gray-50 px-5 py-16 lg:py-30">
      <ProfileSettingsForm
        onDocumentsClick={() => navigate('/profile/documents')}
        onAptitudeTestClick={() => navigate('/profile/aptitude-test')}
      />
    </div>
  );
}
