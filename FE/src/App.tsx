import { Suspense } from 'react';
import { RouterProvider } from 'react-router';
import { AlertViewport, Spinner } from '@/components/feedback';
import AuthSessionSync from '@/features/login/components/AuthSessionSync.tsx';
import QueryProvider from './providers/QueryProvider.tsx';
import { router } from './routes/router.tsx';

export default function App() {
  return (
    <QueryProvider>
      <AuthSessionSync />
      <AlertViewport />
      <Suspense
        fallback={
          <div className="flex min-h-screen items-center justify-center bg-gray-50">
            <Spinner />
          </div>
        }
      >
        <RouterProvider router={router} />
      </Suspense>
    </QueryProvider>
  );
}
