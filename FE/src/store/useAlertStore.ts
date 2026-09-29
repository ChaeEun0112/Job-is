import { create } from 'zustand';

export type AlertVariant = 'success' | 'warning' | 'info' | 'danger';

interface AlertEntry {
  id: number;
  variant: AlertVariant;
  message: string;
  isExiting?: boolean;
}

interface AlertState {
  alerts: AlertEntry[];
  showAlert: (variant: AlertVariant, message: string) => void;
  dismissAlert: (id: number) => void;
}

const ALERT_DURATION = 3000;
const ALERT_EXIT_DURATION = 200;

let nextAlertId = 0;

export const useAlertStore = create<AlertState>((set, get) => ({
  alerts: [],
  showAlert: (variant, message) => {
    const id = nextAlertId++;
    set((state) => ({ alerts: [...state.alerts, { id, variant, message }] }));
    window.setTimeout(() => get().dismissAlert(id), ALERT_DURATION);
  },
  dismissAlert: (id) => {
    const alert = get().alerts.find((item) => item.id === id);
    if (!alert || alert.isExiting) return;

    set((state) => ({
      alerts: state.alerts.map((item) => (item.id === id ? { ...item, isExiting: true } : item)),
    }));
    window.setTimeout(() => {
      set((state) => ({ alerts: state.alerts.filter((item) => item.id !== id) }));
    }, ALERT_EXIT_DURATION);
  },
}));

export const showAlert = (variant: AlertVariant, message: string) =>
  useAlertStore.getState().showAlert(variant, message);
