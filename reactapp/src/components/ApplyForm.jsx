import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSnackContext } from '../context/SnackContext';
import useSnackForm from '../hooks/useSnackForm';
import { formatCurrency, formatExpiry, formatQuantity } from '../utils/formatters';
import './ApplyForm.css';

/**
 * Maximum length of a snack name. It mirrors the limit enforced by the
 * backend validator so the client never submits a name the API would reject.
 */
const NAME_MAX_LENGTH = 60;

/**
 * Field metadata for the form. The ids double as the input `name`, so the
 * submitted payload always matches the contract expected by the backend.
 */
const FIELDS = [
  {
    name: 'snackName',
    label: 'Snack Name:',
    type: 'text',
    placeholder: 'e.g. Roasted Almonds',
    autoComplete: 'off',
    maxLength: NAME_MAX_LENGTH,
  },
  {
    name: 'snackType',
    label: 'Snack Type:',
    type: 'text',
    placeholder: 'e.g. Nuts and Seeds',
    autoComplete: 'off',
  },
  {
    name: 'quantity',
    label: 'Quantity:',
    type: 'number',
    placeholder: 'e.g. 25',
    min: '1',
    step: '1',
  },
  {
    name: 'price',
    label: 'Price:',
    type: 'number',
    placeholder: 'e.g. 149.50',
    min: '0.01',
    step: '0.01',
  },
  {
    name: 'expiryInMonths',
    label: 'Expiry (in months):',
    type: 'number',
    placeholder: 'e.g. 6',
    min: '0',
    step: '1',
  },
];

/**
 * Form used to register a new vegan snack in the catalogue.
 */
function ApplyForm() {
  const navigate = useNavigate();
  const { showToast, refresh } = useSnackContext();

  const { values, errors, submitError, isSubmitting, handleChange, submit } = useSnackForm({
    onSuccess: () => {
      // The toast lives outside the router outlet, so the confirmation stays
      // visible on the page the user is redirected to.
      showToast({
        type: 'success',
        title: 'Snack added',
        message: 'Snack submitted successfully!',
      });

      refresh();
      navigate('/getAllVeganSnacks');
    },
  });

  const nameLength = values.snackName.length;

  return (
    <section className="apply">
      <div className="container apply__inner">
        <div className="apply__main">
          <button
            type="button"
            className="button button--ghost apply__back"
            onClick={() => navigate('/getAllVeganSnacks')}
          >
            <span className="apply__back-arrow" aria-hidden="true">
              ←
            </span>
            Back
          </button>

          <header className="apply__header">
            <p className="section-heading__eyebrow">New catalogue entry</p>
            <h1 className="apply__title">Add a Vegan Snack</h1>
            <p className="apply__lead">
              Capture the details of a vegan snack and it joins the catalogue straight
              away, together with its freshness badge and inventory value.
            </p>
          </header>

          {submitError && (
            <div className="form-banner form-banner--error" role="alert">
              <p className="form-banner__title">We could not add your snack</p>
              <p className="form-banner__message">{submitError}</p>
            </div>
          )}

          <form className="snack-form" onSubmit={submit} noValidate>
            {FIELDS.map((field) => {
              const fieldError = errors[field.name];
              const errorId = `${field.name}-error`;

              return (
                <div
                  className={`form-field${fieldError ? ' form-field--invalid' : ''}`}
                  key={field.name}
                >
                  <div className="form-field__header">
                    <label className="form-field__label" htmlFor={field.name}>
                      {field.label}
                    </label>

                    {field.name === 'snackName' && (
                      <span className="form-field__counter" aria-live="polite">
                        {nameLength}/{NAME_MAX_LENGTH}
                      </span>
                    )}
                  </div>

                  <input
                    className="form-field__input"
                    id={field.name}
                    name={field.name}
                    type={field.type}
                    placeholder={field.placeholder}
                    value={values[field.name]}
                    onChange={handleChange}
                    autoComplete={field.autoComplete}
                    min={field.min}
                    step={field.step}
                    maxLength={field.maxLength}
                    aria-invalid={fieldError ? 'true' : undefined}
                    aria-describedby={fieldError ? errorId : undefined}
                  />

                  {fieldError && (
                    <small className="field-error" id={errorId}>
                      {fieldError}
                    </small>
                  )}
                </div>
              );
            })}

            <div className="snack-form__actions">
              <button className="button button--primary button--large" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Submitting…' : 'Submit'}
              </button>
              <button
                className="button button--ghost"
                type="button"
                onClick={() => navigate('/getAllVeganSnacks')}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>

        <aside className="apply__aside" aria-label="Snack preview">
          <div className="preview-card">
            <p className="preview-card__label">Live preview</p>
            <h2 className="preview-card__title">
              {values.snackName.trim() || 'Your new snack'}
            </h2>
            <p className="preview-card__type">
              {values.snackType.trim() || 'Category not set yet'}
            </p>

            <dl className="preview-card__list">
              <div className="preview-card__row">
                <dt>Quantity</dt>
                <dd>{values.quantity === '' ? '—' : formatQuantity(values.quantity)}</dd>
              </div>
              <div className="preview-card__row">
                <dt>Price</dt>
                <dd>{values.price === '' ? '—' : formatCurrency(values.price)}</dd>
              </div>
              <div className="preview-card__row">
                <dt>Expiry</dt>
                <dd>
                  {values.expiryInMonths === '' ? '—' : formatExpiry(values.expiryInMonths)}
                </dd>
              </div>
            </dl>

            <p className="preview-card__hint">
              The preview updates as you type and mirrors how the snack appears in the
              catalogue.
            </p>
          </div>
        </aside>
      </div>
    </section>
  );
}

export default ApplyForm;
