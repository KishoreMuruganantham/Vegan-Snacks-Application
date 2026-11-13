/**
 * Pure validation rules for the "add a snack" form.
 *
 * The functions here contain no React specific code so they can be unit tested
 * in isolation and reused by any consumer of the form.
 */

export const VALIDATION_MESSAGES = {
  snackName: 'Snack Name is required',
  snackType: 'Snack Type is required',
  quantity: 'Quantity is required',
  priceRequired: 'Price is required',
  pricePositive: 'Price must be a positive number',
  expiryRequired: 'Expiry in months is required',
  expiryNegative: 'Expiry must be a non-negative number',
};

/** The empty shape of the form, also used to reset it after a submission. */
export const INITIAL_SNACK_FORM_VALUES = {
  snackName: '',
  snackType: '',
  quantity: '',
  price: '',
  expiryInMonths: '',
};

export const SNACK_FORM_FIELDS = Object.keys(INITIAL_SNACK_FORM_VALUES);

const readField = (value) => {
  if (value === null || value === undefined) return '';
  return String(value).trim();
};

/**
 * Validates the form values and returns the normalised payload.
 *
 * @param {Record<string, unknown>} values Raw form values.
 * @returns {{ isValid: boolean, errors: Record<string, string>, values: Record<string, unknown> }}
 */
export const validateSnackForm = (values = {}) => {
  const errors = {};

  const snackName = readField(values.snackName);
  const snackType = readField(values.snackType);
  const quantity = readField(values.quantity);
  const price = readField(values.price);
  const expiryInMonths = readField(values.expiryInMonths);

  if (!snackName) errors.snackName = VALIDATION_MESSAGES.snackName;
  if (!snackType) errors.snackType = VALIDATION_MESSAGES.snackType;
  if (!quantity) errors.quantity = VALIDATION_MESSAGES.quantity;

  if (!price) {
    errors.price = VALIDATION_MESSAGES.priceRequired;
  } else if (!Number.isFinite(Number(price)) || Number(price) <= 0) {
    errors.price = VALIDATION_MESSAGES.pricePositive;
  }

  if (!expiryInMonths) {
    errors.expiryInMonths = VALIDATION_MESSAGES.expiryRequired;
  } else if (!Number.isFinite(Number(expiryInMonths)) || Number(expiryInMonths) < 0) {
    errors.expiryInMonths = VALIDATION_MESSAGES.expiryNegative;
  }

  const isValid = Object.keys(errors).length === 0;

  return {
    isValid,
    errors,
    values: {
      snackName,
      snackType,
      quantity: Number(quantity),
      price: Number(price),
      expiryInMonths: Number(expiryInMonths),
    },
  };
};

export default validateSnackForm;
