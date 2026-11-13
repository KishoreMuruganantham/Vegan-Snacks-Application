import { useEffect, useState } from 'react';

/**
 * Returns a value that only updates after `delay` milliseconds have passed
 * without the incoming value changing. Used to keep the search box responsive
 * while avoiding a filter pass on every keystroke.
 *
 * @param {*} value The value to debounce.
 * @param {number} [delay] Debounce window in milliseconds.
 */
const useDebouncedValue = (value, delay = 300) => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedValue(value), delay);
    return () => window.clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
};

export default useDebouncedValue;
