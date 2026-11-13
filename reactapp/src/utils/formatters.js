/**
 * Presentation helpers shared by the feature components.
 *
 * Every helper is defensive: missing or malformed values resolve to an em dash
 * rather than `undefined`, `NaN` or `[object Object]` reaching the DOM.
 */

export const CURRENCY_SYMBOL = '₹';
export const EMPTY_VALUE = '—';

export const FRESHNESS_LABELS = {
  FRESH: 'Fresh',
  EXPIRING_SOON: 'Expiring soon',
  EXPIRED: 'Expired',
  SHELF_STABLE: 'Shelf stable',
};

export const PRICE_BAND_LABELS = {
  BUDGET: 'Budget',
  STANDARD: 'Standard',
  PREMIUM: 'Premium',
};

const MONTH_NAMES = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

/** Returns true only for values that can be safely rendered as a number. */
const isUsableNumber = (value) => {
  if (value === null || value === undefined || value === '') return false;
  return Number.isFinite(Number(value));
};

/** Inserts thousands separators without relying on the host locale. */
const groupThousands = (wholeNumber) =>
  wholeNumber.replace(/\B(?=(\d{3})+(?!\d))/g, ',');

/**
 * Formats a monetary amount, e.g. 149.5 -> "₹149.50".
 * @param {number|string} value
 * @param {{ symbol?: string }} [options]
 */
export const formatCurrency = (value, options = {}) => {
  const symbol = options.symbol || CURRENCY_SYMBOL;
  if (!isUsableNumber(value)) return EMPTY_VALUE;
  const [whole, fraction] = Number(value).toFixed(2).split('.');
  return `${symbol}${groupThousands(whole)}.${fraction}`;
};

/**
 * Formats an ISO date (or Date instance) as "13 May 2026".
 * Date-only strings are parsed textually so the day never shifts with the
 * viewer's timezone.
 */
export const formatDate = (value) => {
  if (value === null || value === undefined || value === '') return EMPTY_VALUE;

  if (value instanceof Date) {
    if (Number.isNaN(value.getTime())) return EMPTY_VALUE;
    return `${value.getDate()} ${MONTH_NAMES[value.getMonth()]} ${value.getFullYear()}`;
  }

  const raw = String(value).trim();
  const dateOnlyMatch = /^(\d{4})-(\d{2})-(\d{2})/.exec(raw);
  if (dateOnlyMatch) {
    const [, year, month, day] = dateOnlyMatch;
    return `${Number(day)} ${MONTH_NAMES[Number(month) - 1]} ${year}`;
  }

  const parsed = new Date(raw);
  if (Number.isNaN(parsed.getTime())) return EMPTY_VALUE;
  return `${parsed.getDate()} ${MONTH_NAMES[parsed.getMonth()]} ${parsed.getFullYear()}`;
};

/**
 * Formats the shelf life, e.g. 6 -> "6 months", 1 -> "1 month".
 */
export const formatExpiry = (months) => {
  if (!isUsableNumber(months)) return EMPTY_VALUE;
  const value = Number(months);
  return `${value} ${value === 1 ? 'month' : 'months'}`;
};

/** Formats a stock quantity, keeping the raw number easy to scan. */
export const formatQuantity = (quantity) => {
  if (!isUsableNumber(quantity)) return EMPTY_VALUE;
  return groupThousands(String(Number(quantity).toFixed(0)));
};

/**
 * Resolves the expiry date of a snack, preferring the value supplied by the
 * backend and falling back to the creation date plus the shelf life.
 */
export const resolveExpiryDate = (snack) => {
  if (!snack || typeof snack !== 'object') return null;
  if (snack.expiryDate) return snack.expiryDate;
  if (!isUsableNumber(snack.expiryInMonths)) return null;

  const base = snack.createdAt ? new Date(snack.createdAt) : new Date();
  if (Number.isNaN(base.getTime())) return null;

  const expiry = new Date(base.getTime());
  expiry.setMonth(expiry.getMonth() + Number(snack.expiryInMonths));
  return expiry;
};

/** Humanises an UPPER_SNAKE_CASE or UPPER CASE token, e.g. NUTS_AND_SEEDS. */
export const humanizeToken = (value) => {
  if (value === null || value === undefined) return EMPTY_VALUE;
  const raw = String(value).trim();
  if (!raw) return EMPTY_VALUE;

  const source = raw.includes('_') ? raw.toLowerCase() : raw;
  const isScreaming = source === source.toUpperCase() && /[A-Z]/.test(source);

  if (!raw.includes('_') && !isScreaming) return raw;

  return source
    .split(/[\s_]+/)
    .filter(Boolean)
    .map((word) => (word === 'and' ? '&' : word.charAt(0).toUpperCase() + word.slice(1)))
    .join(' ');
};

/**
 * Returns the friendly label for a snack category or type.
 * Values that already read well are returned untouched.
 */
export const categoryLabel = (value) => {
  if (value === null || value === undefined) return EMPTY_VALUE;
  const raw = String(value).trim();
  if (!raw) return EMPTY_VALUE;

  if (raw.includes('_')) return humanizeToken(raw);
  if (raw === raw.toUpperCase() && /[A-Z]/.test(raw)) return humanizeToken(raw);
  return raw;
};

/** Returns the display label for a freshness status. */
export const formatFreshness = (status) => {
  if (!status) return EMPTY_VALUE;
  const key = String(status).toUpperCase();
  if (FRESHNESS_LABELS[key]) return FRESHNESS_LABELS[key];
  return humanizeToken(status);
};

/** Returns a CSS friendly key for a freshness status (fresh, expired, ...). */
export const freshnessKey = (status) => {
  if (!status) return 'unknown';
  return String(status).toUpperCase().toLowerCase().replace(/_/g, '-');
};

/** Returns the display label for a price band. */
export const formatPriceBand = (band) => {
  if (!band) return EMPTY_VALUE;
  const key = String(band).toUpperCase();
  if (PRICE_BAND_LABELS[key]) return PRICE_BAND_LABELS[key];
  return humanizeToken(band);
};

/** Returns a CSS friendly key for a price band (budget, standard, ...). */
export const priceBandKey = (band) => {
  if (!band) return 'unknown';
  return String(band).toUpperCase().toLowerCase().replace(/_/g, '-');
};

/**
 * Derives the inventory statistics for a list of snacks.
 * Shared by the context provider and the catalogue so both agree on the numbers.
 */
export const summariseSnacks = (snacks) => {
  const list = Array.isArray(snacks) ? snacks : [];

  let totalQuantity = 0;
  let totalValue = 0;

  list.forEach((snack) => {
    const source = snack || {};
    const quantity = Number(source.quantity);
    const price = Number(source.price);
    const explicitValue = Number(source.totalValue);

    const safeQuantity = Number.isFinite(quantity) ? quantity : 0;
    totalQuantity += safeQuantity;

    if (Number.isFinite(explicitValue)) {
      totalValue += explicitValue;
    } else if (Number.isFinite(price)) {
      totalValue += price * safeQuantity;
    }
  });

  return {
    totalCount: list.length,
    totalQuantity,
    totalValue,
  };
};
