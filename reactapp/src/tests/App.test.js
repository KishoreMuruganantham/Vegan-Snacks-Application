import React from 'react';
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { API_BASE_URL, API_ENDPOINTS } from '../apiConfig';

const SAMPLE_SNACKS = [
  {
    id: 1,
    snackName: 'Roasted Almonds',
    snackType: 'Nuts and Seeds',
    category: 'NUTS_AND_SEEDS',
    quantity: 25,
    price: 149.5,
    expiryInMonths: 6,
    expiryDate: '2027-05-13',
    freshnessStatus: 'FRESH',
    priceBand: 'STANDARD',
    totalValue: 3737.5,
    isVegan: true,
    createdAt: '2026-11-13T05:20:31.123',
  },
  {
    id: 2,
    snackName: 'Cocoa Energy Bites',
    snackType: 'Energy Bars',
    category: 'ENERGY_BARS',
    quantity: 12,
    price: 199,
    expiryInMonths: 3,
    expiryDate: '2027-02-10',
    freshnessStatus: 'EXPIRING_SOON',
    priceBand: 'PREMIUM',
    totalValue: 2388,
    isVegan: true,
    createdAt: '2026-11-10T09:00:00.000',
  },
];

/**
 * The URL of the submission endpoint. It is built from the shared API
 * configuration so that the test follows the client wherever it is pointed.
 * During the test run the configuration resolves to the local Spring Boot API.
 */
const ADD_SNACK_URL = `${API_BASE_URL}${API_ENDPOINTS.ADD_SNACK}`;

/** Builds a minimal Response-like object for the mocked fetch. */
const buildResponse = (payload, status = 200) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => payload,
  text: async () => JSON.stringify(payload),
  headers: { get: () => 'application/json' },
});

beforeEach(() => {
  global.fetch = jest.fn(async (url, options = {}) =>
    options.method === 'POST' ? buildResponse(SAMPLE_SNACKS[0]) : buildResponse(SAMPLE_SNACKS)
  );
});

afterEach(() => {
  jest.clearAllMocks();
});

/**
 * Renders the application and flushes the catalogue request that starts on
 * mount, so no state update escapes the test.
 */
const renderApp = async () => {
  await act(async () => {
    render(<App />);
  });
};

/** Clicks an element and waits for the resulting route change to settle. */
const clickElement = async (user, element) => {
  await act(async () => {
    await user.click(element);
  });
};

/** Types into a field while keeping the state updates inside the act scope. */
const typeInto = async (user, element, text) => {
  await act(async () => {
    await user.type(element, text);
  });
};

/** Navigates to the apply form through the primary navigation. */
const goToApplyForm = async (user) => {
  const navigation = screen.getByRole('navigation');
  await clickElement(user, within(navigation).getByRole('link', { name: 'Add a Snack' }));
};

describe('Vegan Snacks Application', () => {
  test('renders the home page heading and the primary call to action', async () => {
    await renderApp();

    expect(
      screen.getByRole('heading', { name: 'Welcome to the Vegan Snacks App!' })
    ).toBeInTheDocument();

    const callToAction = screen.getByRole('link', { name: 'Add Your Vegan Snack' });
    expect(callToAction).toBeInTheDocument();
    expect(callToAction).toHaveAttribute('href', '/apply');
  });

  test('navigates to the apply form and exposes every labelled field', async () => {
    const user = userEvent.setup();
    await renderApp();

    await goToApplyForm(user);

    expect(screen.getByRole('heading', { name: 'Add a Vegan Snack' })).toBeInTheDocument();

    expect(screen.getByLabelText('Snack Name:')).toBeInTheDocument();
    expect(screen.getByLabelText('Snack Type:')).toBeInTheDocument();
    expect(screen.getByLabelText('Quantity:')).toBeInTheDocument();
    expect(screen.getByLabelText('Price:')).toBeInTheDocument();
    expect(screen.getByLabelText('Expiry (in months):')).toBeInTheDocument();

    expect(screen.getByLabelText('Snack Name:')).toHaveAttribute('id', 'snackName');
    expect(screen.getByLabelText('Snack Type:')).toHaveAttribute('id', 'snackType');
    expect(screen.getByLabelText('Quantity:')).toHaveAttribute('id', 'quantity');
    expect(screen.getByLabelText('Price:')).toHaveAttribute('id', 'price');
    expect(screen.getByLabelText('Expiry (in months):')).toHaveAttribute('id', 'expiryInMonths');
  });

  test('shows every validation message when an empty form is submitted', async () => {
    const user = userEvent.setup();
    await renderApp();

    await goToApplyForm(user);
    await clickElement(user, screen.getByRole('button', { name: 'Submit' }));

    expect(screen.getByText('Snack Name is required')).toBeInTheDocument();
    expect(screen.getByText('Snack Type is required')).toBeInTheDocument();
    expect(screen.getByText('Quantity is required')).toBeInTheDocument();
    expect(screen.getByText('Price is required')).toBeInTheDocument();
    expect(screen.getByText('Expiry in months is required')).toBeInTheDocument();

    expect(screen.getByLabelText('Snack Name:')).toHaveAttribute('aria-invalid', 'true');
    expect(screen.getByLabelText('Price:')).toHaveAttribute('aria-invalid', 'true');
    expect(global.fetch).not.toHaveBeenCalledWith(
      ADD_SNACK_URL,
      expect.objectContaining({ method: 'POST' })
    );
  });

  test('rejects a non positive price and a negative expiry', async () => {
    const user = userEvent.setup();
    await renderApp();

    await goToApplyForm(user);

    await typeInto(user, screen.getByLabelText('Snack Name:'), 'Roasted Almonds');
    await typeInto(user, screen.getByLabelText('Snack Type:'), 'Nuts and Seeds');
    await typeInto(user, screen.getByLabelText('Quantity:'), '25');
    fireEvent.change(screen.getByLabelText('Price:'), { target: { value: '0' } });
    fireEvent.change(screen.getByLabelText('Expiry (in months):'), { target: { value: '-2' } });

    await clickElement(user, screen.getByRole('button', { name: 'Submit' }));

    expect(screen.getByText('Price must be a positive number')).toBeInTheDocument();
    expect(screen.getByText('Expiry must be a non-negative number')).toBeInTheDocument();
  });

  test('submits a valid snack and confirms the submission', async () => {
    const user = userEvent.setup();
    await renderApp();

    await goToApplyForm(user);

    await typeInto(user, screen.getByLabelText('Snack Name:'), 'Roasted Almonds');
    await typeInto(user, screen.getByLabelText('Snack Type:'), 'Nuts and Seeds');
    await typeInto(user, screen.getByLabelText('Quantity:'), '25');
    fireEvent.change(screen.getByLabelText('Price:'), { target: { value: '149.50' } });
    await typeInto(user, screen.getByLabelText('Expiry (in months):'), '6');

    await clickElement(user, screen.getByRole('button', { name: 'Submit' }));

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        ADD_SNACK_URL,
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
          body: expect.stringContaining('"snackName":"Roasted Almonds"'),
        })
      );
    });

    expect(await screen.findByText('Snack submitted successfully!')).toBeInTheDocument();

    // The route changed and the confirmation is still visible on the catalogue.
    expect(screen.getByRole('heading', { name: 'Your Vegan Snack Shelf' })).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Snack submitted successfully!');
  });

  test('renders the application title and the primary navigation links', async () => {
    await renderApp();

    const navigation = screen.getByRole('navigation');

    expect(within(navigation).getByText('Vegan Snacks Application')).toBeInTheDocument();
    expect(within(navigation).getByRole('link', { name: 'Home' })).toHaveAttribute('href', '/');
    expect(within(navigation).getByRole('link', { name: 'Snacks Details' })).toHaveAttribute(
      'href',
      '/getAllVeganSnacks'
    );
  });

  test('renders the footer copyright notice', async () => {
    await renderApp();

    expect(
      screen.getByText('© 2024 Vegan Snacks Application. All rights reserved.')
    ).toBeInTheDocument();
  });

  test('addresses the backend through the shared endpoint configuration', () => {
    // The endpoints keep the names of the problem statement, so the client
    // reaches the same API whether it runs locally or on a hosting platform.
    expect(API_ENDPOINTS.GET_ALL_SNACKS).toContain('/getAllVeganSnacks');
    expect(API_ENDPOINTS.ADD_SNACK).toContain('/addVeganSnack');
    expect(API_ENDPOINTS.STATS).toContain('/api/v1/snacks/stats');
    expect(API_ENDPOINTS.HEALTH).toContain('/api/v1/health');

    // The test runs on a development machine, so the configuration points at
    // the Spring Boot API on port 8080.
    expect(API_BASE_URL).toBe('http://localhost:8080');
  });

  test('lists the catalogue with formatted values', async () => {
    const user = userEvent.setup();
    await renderApp();

    const navigation = screen.getByRole('navigation');
    await clickElement(user, within(navigation).getByRole('link', { name: 'Snacks Details' }));

    expect(await screen.findByText('Roasted Almonds')).toBeInTheDocument();
    expect(screen.getByText('Cocoa Energy Bites')).toBeInTheDocument();
    expect(screen.getByText('₹149.50')).toBeInTheDocument();
    // The type appears on the card and in the type filter, hence the plural query.
    expect(screen.getAllByText('Nuts and Seeds').length).toBeGreaterThan(0);
    expect(screen.getByText('6 months')).toBeInTheDocument();
  });
});
