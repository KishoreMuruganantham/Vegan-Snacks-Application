import { useCallback, useState } from 'react';
import { API_ENDPOINTS, buildApiUrl } from '../apiConfig';
import { INITIAL_SNACK_FORM_VALUES, validateSnackForm } from '../utils/validators';

const SUBMIT_FAILED_MESSAGE = 'Something went wrong while submitting the snack. Please try again.';

/**
 * Reads the body of a failed request and maps it onto a message plus the
 * optional field level errors returned by the backend.
 */
const readErrorPayload = async (response) => {
  const fallback = `The request failed with status ${response.status}.`;

  try {
    const contentType =
      (response.headers && typeof response.headers.get === 'function'
        ? response.headers.get('content-type')
        : '') || '';

    if (contentType.includes('application/json')) {
      const body = await response.json();
      if (body && typeof body === 'object') {
        return {
          message: body.message || body.error || fallback,
          fieldErrors: body.fieldErrors || null,
        };
      }
      return { message: fallback, fieldErrors: null };
    }

    if (typeof response.text === 'function') {
      const text = await response.text();
      return { message: text || fallback, fieldErrors: null };
    }

    return { message: fallback, fieldErrors: null };
  } catch (parseError) {
    return { message: fallback, fieldErrors: null };
  }
};

/**
 * Owns the state of the "add a vegan snack" form: values, validation errors,
 * submission status and the POST request itself.
 *
 * @param {{ onSuccess?: Function }} [options]
 */
const useSnackForm = (options = {}) => {
  const { onSuccess } = options;

  const [values, setValues] = useState(INITIAL_SNACK_FORM_VALUES);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState(null);
  const [status, setStatus] = useState('idle');

  const isSubmitting = status === 'submitting';

  const reset = useCallback(() => {
    setValues(INITIAL_SNACK_FORM_VALUES);
    setErrors({});
    setSubmitError(null);
    setStatus('idle');
  }, []);

  const handleChange = useCallback((event) => {
    const { name, value } = event.target;

    setValues((previous) => ({ ...previous, [name]: value }));

    // Clear a field error as soon as the user starts correcting the field.
    setErrors((previous) => {
      if (!previous[name]) return previous;
      const next = { ...previous };
      delete next[name];
      return next;
    });
  }, []);

  const submit = useCallback(
    async (event) => {
      if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
      }

      const validation = validateSnackForm(values);
      setErrors(validation.errors);
      setSubmitError(null);

      if (!validation.isValid) {
        setStatus('invalid');
        return { ok: false, errors: validation.errors };
      }

      setStatus('submitting');

      try {
        const response = await fetch(buildApiUrl(API_ENDPOINTS.ADD_SNACK), {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(validation.values),
        });

        if (!response.ok) {
          const { message, fieldErrors } = await readErrorPayload(response);
          const requestError = new Error(message || SUBMIT_FAILED_MESSAGE);
          requestError.fieldErrors = fieldErrors;
          throw requestError;
        }

        let createdSnack = null;
        try {
          createdSnack = await response.json();
        } catch (parseError) {
          createdSnack = null;
        }

        setStatus('success');
        reset();

        if (typeof onSuccess === 'function') {
          onSuccess(createdSnack);
        }

        return { ok: true, data: createdSnack };
      } catch (requestError) {
        console.error('Unable to submit the vegan snack:', requestError);

        setStatus('error');
        setSubmitError((requestError && requestError.message) || SUBMIT_FAILED_MESSAGE);

        if (requestError && requestError.fieldErrors) {
          setErrors((previous) => ({ ...previous, ...requestError.fieldErrors }));
        }

        return { ok: false, error: requestError };
      }
    },
    [onSuccess, reset, values]
  );

  return {
    values,
    errors,
    submitError,
    status,
    isSubmitting,
    handleChange,
    submit,
    reset,
  };
};

export default useSnackForm;
