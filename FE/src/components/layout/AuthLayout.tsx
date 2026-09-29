import { Outlet, useLocation } from 'react-router';
import useRouteMetadata from '@/hooks/useRouteMetadata';

export default function AuthLayout() {
  useRouteMetadata();
  const location = useLocation();

  return (
    <div className="flex min-h-screen flex-col overflow-x-clip">
      <div key={location.pathname} className="page-content-enter flex min-h-0 flex-1 flex-col">
        <Outlet />
      </div>
    </div>
  );
}
