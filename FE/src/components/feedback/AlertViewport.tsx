import { useAlertStore } from '@/store/useAlertStore';
import Alert from './Alert';

export default function AlertViewport() {
  const alerts = useAlertStore((state) => state.alerts);

  if (alerts.length === 0) return null;

  return (
    <div className="fixed top-24 left-1/2 z-50 flex w-full max-w-103.5 -translate-x-1/2 flex-col gap-2 px-3">
      {alerts.map((alert) => (
        <Alert
          key={alert.id}
          variant={alert.variant}
          size="slim"
          className={
            alert.isExiting
              ? 'motion-safe:animate-[alert-toast-exit_200ms_ease-in_both]'
              : 'motion-safe:animate-[alert-toast-enter_240ms_cubic-bezier(0.16,1,0.3,1)_both]'
          }
        >
          {alert.message}
        </Alert>
      ))}
    </div>
  );
}
