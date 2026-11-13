import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import useSnacks from '../hooks/useSnacks';
import { summariseSnacks } from '../utils/formatters';

/**
 * Lifetime of a toast notification in milliseconds. Long enough to read and
 * act on the message, short enough not to linger on the screen.
 */
const TOAST_DURATION = 10000;

const SnackContext = createContext(null);

/**
 * Provides the shared catalogue state (snacks, loading, error, refresh), the
 * derived inventory statistics and the global notification channel used to
 * surface messages that must survive a route change.
 */
export function SnackProvider({ children }) {
  const { snacks, loading, error, refresh } = useSnacks();

  const [toast, setToast] = useState(null);
  const toastTimerRef = useRef(null);
  const toastCounterRef = useRef(0);

  const dismissToast = useCallback(() => {
    if (toastTimerRef.current) {
      window.clearTimeout(toastTimerRef.current);
      toastTimerRef.current = null;
    }
    setToast(null);
  }, []);

  const showToast = useCallback((notification) => {
    const payload =
      typeof notification === 'string' ? { message: notification } : notification || {};

    toastCounterRef.current += 1;

    setToast({
      id: toastCounterRef.current,
      type: payload.type || 'info',
      title: payload.title || '',
      message: payload.message || '',
    });

    if (toastTimerRef.current) {
      window.clearTimeout(toastTimerRef.current);
    }

    toastTimerRef.current = window.setTimeout(() => {
      toastTimerRef.current = null;
      setToast(null);
    }, TOAST_DURATION);
  }, []);

  useEffect(
    () => () => {
      if (toastTimerRef.current) {
        window.clearTimeout(toastTimerRef.current);
      }
    },
    []
  );

  const stats = useMemo(() => summariseSnacks(snacks), [snacks]);

  const contextValue = useMemo(
    () => ({
      snacks,
      loading,
      error,
      refresh,
      stats,
      toast,
      showToast,
      dismissToast,
    }),
    [snacks, loading, error, refresh, stats, toast, showToast, dismissToast]
  );

  return <SnackContext.Provider value={contextValue}>{children}</SnackContext.Provider>;
}

/**
 * Access the shared catalogue state. Throws when used outside the provider so
 * that wiring mistakes surface immediately during development.
 */
export function useSnackContext() {
  const context = useContext(SnackContext);

  if (!context) {
    throw new Error('useSnackContext must be used within a SnackProvider.');
  }

  return context;
}

/**
 * Catalogue state for page components.
 *
 * Inside the application the shared catalogue is used, so the page never
 * triggers a second request. A page rendered on its own falls back to its own
 * request, which keeps the component usable in isolation.
 */
export function useCatalogue() {
  const context = useContext(SnackContext);
  const local = useSnacks(!context);

  if (context) {
    return context;
  }

  return {
    ...local,
    stats: summariseSnacks(local.snacks),
    toast: null,
    showToast: () => {},
    dismissToast: () => {},
  };
}

export default SnackProvider;
