import { useCallback, useEffect, useRef, useState } from 'react';
import { API_ENDPOINTS, buildApiUrl } from '../apiConfig';

const REQUEST_FAILED_MESSAGE = 'Unable to load the vegan snack catalogue right now.';

/**
 * Loads the snack catalogue from the backend.
 *
 * Requests are issued through an AbortController so that a refresh triggered
 * while a previous request is still in flight does not overwrite newer data,
 * and so that pending requests are cancelled when the component unmounts.
 *
 * @param {boolean} [enabled] When false the hook stays idle and issues no
 *   request. This lets a component reuse the catalogue that the provider
 *   already loaded instead of fetching it a second time.
 * @returns {{ snacks: Array<object>, loading: boolean, error: string|null, refresh: Function }}
 */
const useSnacks = (enabled = true) => {
  const [snacks, setSnacks] = useState([]);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState(null);
  const abortControllerRef = useRef(null);

  const refresh = useCallback(async () => {
    if (!enabled) return;

    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    const controller = new AbortController();
    abortControllerRef.current = controller;

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.GET_ALL_SNACKS), {
        method: 'GET',
        headers: { Accept: 'application/json' },
        signal: controller.signal,
      });

      if (!response.ok) {
        throw new Error(`The catalogue request failed with status ${response.status}.`);
      }

      const payload = await response.json();
      if (controller.signal.aborted) return;

      setSnacks(Array.isArray(payload) ? payload : []);
    } catch (requestError) {
      // An aborted request is an expected part of refresh flows, not a failure.
      if (requestError && requestError.name === 'AbortError') return;

      console.error('Unable to fetch the vegan snack catalogue:', requestError);
      setError((requestError && requestError.message) || REQUEST_FAILED_MESSAGE);
    } finally {
      if (!controller.signal.aborted) {
        setLoading(false);
      }
    }
  }, [enabled]);

  useEffect(() => {
    if (!enabled) return undefined;

    refresh();

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [refresh, enabled]);

  return { snacks, loading, error, refresh };
};

export default useSnacks;
