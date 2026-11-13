/**
 * Catalogue of the hosted deployment.
 *
 * The functions in this folder implement the same contract as the Spring Boot
 * API of the `springapp` module, including the validation messages, the
 * category resolution and the derived pricing, freshness and valuation
 * figures. The catalogue itself is held in memory, which keeps the deployment
 * free of any external database.
 *
 * Files whose name starts with an underscore are not published as routes, so
 * this module stays internal to the functions.
 */

/** Catalogue categories of the application. */
const CATEGORY = {
  CHIPS_AND_CRUNCHIES: 'CHIPS_AND_CRUNCHIES',
  NUTS_AND_SEEDS: 'NUTS_AND_SEEDS',
  FRUIT_SNACKS: 'FRUIT_SNACKS',
  CHOCOLATE: 'CHOCOLATE',
  ENERGY_BARS: 'ENERGY_BARS',
  BAKED_TREATS: 'BAKED_TREATS',
  OTHERS: 'OTHERS',
};

/** Keywords that map a free text snack type onto a catalogue category. */
const CATEGORY_KEYWORDS = {
  [CATEGORY.CHOCOLATE]: ['chocolate', 'cocoa'],
  [CATEGORY.FRUIT_SNACKS]: ['fruit'],
  [CATEGORY.NUTS_AND_SEEDS]: ['nut', 'seed'],
  [CATEGORY.ENERGY_BARS]: ['bar', 'energy'],
  [CATEGORY.BAKED_TREATS]: ['cake', 'biscuit', 'cookie', 'bread', 'cracker', 'baked'],
  [CATEGORY.CHIPS_AND_CRUNCHIES]: ['chip', 'crunch', 'extruded'],
};

/** Order in which the categories are evaluated during the keyword resolution. */
const RESOLUTION_ORDER = [
  CATEGORY.CHOCOLATE,
  CATEGORY.FRUIT_SNACKS,
  CATEGORY.NUTS_AND_SEEDS,
  CATEGORY.ENERGY_BARS,
  CATEGORY.BAKED_TREATS,
  CATEGORY.CHIPS_AND_CRUNCHIES,
];

/** Limits accepted by the validator. */
const LIMITS = {
  minNameLength: 2,
  maxNameLength: 60,
  maxTypeLength: 40,
  minQuantity: 1,
  maxQuantity: 100000,
  maxPrice: 1000000,
  maxPriceScale: 2,
  maxExpiryMonths: 120,
};

/** Characters accepted inside a snack name. */
const NAME_PATTERN = /^[A-Za-z0-9 &().,'/+\-%_#:]+$/;

/** The reference snacks every fresh deployment starts with. */
const SEED_SNACKS = [
  { snackName: 'Roasted Almonds', snackType: 'Nuts and Seeds', quantity: 25, price: 149.5, expiryInMonths: 6 },
  { snackName: 'Banana Chips', snackType: 'Chips', quantity: 40, price: 79.99, expiryInMonths: 4 },
  { snackName: 'Dark Chocolate Bar', snackType: 'Chocolate', quantity: 18, price: 199.0, expiryInMonths: 12 },
  { snackName: 'Dried Mango Slices', snackType: 'Fruit Snacks', quantity: 30, price: 249.75, expiryInMonths: 9 },
  { snackName: 'Quinoa Energy Bar', snackType: 'Energy Bars', quantity: 60, price: 89.0, expiryInMonths: 8 },
  { snackName: 'Multigrain Crackers', snackType: 'Baked Treats', quantity: 45, price: 129.0, expiryInMonths: 5 },
];

/** The catalogue of the deployment. */
const catalogue = [];
let nextId = 1;

/**
 * Adds a month to a date, clamping the day to the length of the target month
 * so that the 31st of January becomes the end of February.
 *
 * @param {Date} date the starting point
 * @param {number} months the number of months to add
 * @returns {Date} the resulting date
 */
const addMonths = (date, months) => {
  const result = new Date(date.getTime());
  const targetDay = result.getDate();

  result.setDate(1);
  result.setMonth(result.getMonth() + months);
  const lastDay = new Date(result.getFullYear(), result.getMonth() + 1, 0).getDate();

  result.setDate(Math.min(targetDay, lastDay));
  return result;
};

/**
 * Formats a date as an ISO calendar date.
 *
 * @param {Date} date the date to format
 * @returns {string} the date in the `YYYY-MM-DD` format
 */
const toIsoDate = (date) => date.toISOString().slice(0, 10);

/**
 * Rounds a monetary amount to two decimal places.
 *
 * @param {number} amount the amount to round
 * @returns {number} the rounded amount
 */
const roundMoney = (amount) => Math.round((amount + Number.EPSILON) * 100) / 100;

/**
 * Resolves the catalogue category of a free text snack type.
 *
 * @param {string} snackType the free text description
 * @returns {string} the matching category
 */
const resolveCategory = (snackType) => {
  if (!snackType) return CATEGORY.OTHERS;

  const normalised = String(snackType).trim().toLowerCase();
  if (!normalised) return CATEGORY.OTHERS;

  for (const category of RESOLUTION_ORDER) {
    if (CATEGORY_KEYWORDS[category].some((keyword) => normalised.includes(keyword))) {
      return category;
    }
  }
  return CATEGORY.OTHERS;
};

/**
 * Resolves the price band of a product.
 *
 * @param {number} price the unit price
 * @returns {string} the name of the band
 */
const priceBand = (price) => {
  if (price < 100) return 'BUDGET';
  if (price < 500) return 'STANDARD';
  if (price < 2000) return 'PREMIUM';
  return 'ARTISAN';
};

/**
 * Resolves the freshness status of a product from its remaining shelf life.
 *
 * @param {number} expiryInMonths the remaining shelf life in months
 * @returns {string} the name of the status
 */
const freshnessStatus = (expiryInMonths) => {
  if (expiryInMonths <= 0) return 'SHELF_STABLE';
  if (expiryInMonths <= 2) return 'EXPIRING_SOON';
  if (expiryInMonths <= 11) return 'FRESH';
  return 'SHELF_STABLE';
};

/**
 * Validates a submitted payload and collects every problem it finds.
 *
 * @param {object} payload the submitted payload
 * @returns {object} a map of field name to error message
 */
const validate = (payload) => {
  const errors = {};

  if (!payload || typeof payload !== 'object') {
    return {
      snackName: 'Snack Name is required',
      snackType: 'Snack Type is required',
      quantity: 'Quantity is required',
      price: 'Price is required',
      expiryInMonths: 'Expiry in months is required',
    };
  }

  const snackName = typeof payload.snackName === 'string' ? payload.snackName.trim() : '';
  const snackType = typeof payload.snackType === 'string' ? payload.snackType.trim() : '';

  if (!snackName) {
    errors.snackName = 'Snack Name is required';
  } else if (snackName.length < LIMITS.minNameLength || snackName.length > LIMITS.maxNameLength) {
    errors.snackName = 'Snack Name must be between 2 and 60 characters';
  } else if (!NAME_PATTERN.test(snackName)) {
    errors.snackName = 'Snack Name contains unsupported characters';
  }

  if (!snackType) {
    errors.snackType = 'Snack Type is required';
  } else if (snackType.length > LIMITS.maxTypeLength) {
    errors.snackType = 'Snack Type must be 40 characters or less';
  }

  const quantity = payload.quantity;
  if (quantity === undefined || quantity === null || quantity === '') {
    errors.quantity = 'Quantity is required';
  } else if (!Number.isInteger(Number(quantity)) || Number(quantity) < LIMITS.minQuantity) {
    errors.quantity = 'Quantity must be at least 1';
  } else if (Number(quantity) > LIMITS.maxQuantity) {
    errors.quantity = 'Quantity must be 100000 or less';
  }

  const price = payload.price;
  if (price === undefined || price === null || price === '') {
    errors.price = 'Price is required';
  } else if (!Number.isFinite(Number(price)) || Number(price) <= 0) {
    errors.price = 'Price must be a positive number';
  } else if (Number(price) > LIMITS.maxPrice) {
    errors.price = 'Price must be 1000000 or less';
  } else if (String(price).includes('.') && String(price).split('.')[1].length > LIMITS.maxPriceScale) {
    errors.price = 'Price must have at most 2 decimal places';
  }

  const expiryInMonths = payload.expiryInMonths;
  if (expiryInMonths === undefined || expiryInMonths === null || expiryInMonths === '') {
    errors.expiryInMonths = 'Expiry in months is required';
  } else if (!Number.isInteger(Number(expiryInMonths)) || Number(expiryInMonths) < 0) {
    errors.expiryInMonths = 'Expiry must be a non-negative number';
  } else if (Number(expiryInMonths) > LIMITS.maxExpiryMonths) {
    errors.expiryInMonths = 'Expiry must be 120 months or less';
  }

  return errors;
};

/**
 * Converts a catalogue record into its transport representation.
 *
 * @param {object} record the stored record
 * @returns {object} the transport representation
 */
const toResponse = (record) => ({
  id: record.id,
  snackName: record.snackName,
  snackType: record.snackType,
  category: record.category,
  quantity: record.quantity,
  price: record.price,
  expiryInMonths: record.expiryInMonths,
  expiryDate: record.expiryDate,
  freshnessStatus: record.freshnessStatus,
  priceBand: record.priceBand,
  totalValue: record.totalValue,
  isVegan: true,
  createdAt: record.createdAt,
});

/**
 * Fills in the derived figures of a record and stores it.
 *
 * @param {object} payload the validated payload
 * @returns {object} the stored record
 */
const store = (payload) => {
  const snackName = String(payload.snackName).trim().replace(/\s+/g, ' ');
  const snackType = String(payload.snackType).trim().replace(/\s+/g, ' ');
  const quantity = Number(payload.quantity);
  const price = roundMoney(Number(payload.price));
  const expiryInMonths = Number(payload.expiryInMonths);

  const record = {
    id: nextId,
    snackName,
    snackType,
    category: resolveCategory(snackType),
    quantity,
    price,
    expiryInMonths,
    expiryDate: toIsoDate(addMonths(new Date(), expiryInMonths)),
    freshnessStatus: freshnessStatus(expiryInMonths),
    priceBand: priceBand(price),
    totalValue: roundMoney(price * quantity),
    createdAt: new Date().toISOString(),
  };

  nextId += 1;
  catalogue.unshift(record);
  return record;
};

/**
 * Fills the catalogue with the reference snacks the first time it is used.
 */
const seed = () => {
  if (catalogue.length > 0) {
    return;
  }
  SEED_SNACKS.forEach((snack) => store(snack));
};

/**
 * Checks whether a name is already used by another record.
 *
 * @param {string} snackName the name to look for
 * @returns {boolean} true when the name is taken
 */
const nameExists = (snackName) =>
  catalogue.some((record) => record.snackName.toLowerCase() === snackName.toLowerCase());

/**
 * Builds the aggregated figures of the catalogue.
 *
 * @returns {object} the statistics of the catalogue
 */
const buildStats = () => {
  const categories = new Map();
  let totalQuantity = 0;
  let totalInventoryValue = 0;
  let unitPriceSum = 0;
  let expiringSoonCount = 0;
  let expiredCount = 0;

  catalogue.forEach((record) => {
    totalQuantity += record.quantity;
    totalInventoryValue += record.totalValue;
    unitPriceSum += record.price;

    if (record.freshnessStatus === 'EXPIRING_SOON') {
      expiringSoonCount += 1;
    } else if (record.freshnessStatus === 'EXPIRED') {
      expiredCount += 1;
    }

    const current = categories.get(record.category) || { count: 0, totalQuantity: 0, totalValue: 0 };
    current.count += 1;
    current.totalQuantity += record.quantity;
    current.totalValue = roundMoney(current.totalValue + record.totalValue);
    categories.set(record.category, current);
  });

  const averagePrice =
    catalogue.length === 0 ? 0 : roundMoney(unitPriceSum / catalogue.length);

  return {
    totalSnacks: catalogue.length,
    totalQuantity,
    totalInventoryValue: roundMoney(totalInventoryValue),
    averagePrice,
    expiringSoonCount,
    expiredCount,
    byCategory: Object.keys(CATEGORY)
      .filter((category) => categories.has(category))
      .map((category) => {
        const entry = categories.get(category);
        return {
          category,
          count: entry.count,
          totalQuantity: entry.totalQuantity,
          totalValue: entry.totalValue,
        };
      }),
  };
};

/**
 * Headers shared by every response of the API.
 *
 * @returns {object} the response headers
 */
const jsonHeaders = () => ({
  'Content-Type': 'application/json; charset=utf-8',
  'Cache-Control': 'no-store',
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Accept',
});

/**
 * Returns the catalogue in the order it is presented to the client, newest
 * entry first.
 *
 * @returns {Array<object>} the stored records
 */
const catalogueSnapshot = () => catalogue.slice();

module.exports = {
  CATEGORY,
  buildStats,
  catalogueSnapshot,
  jsonHeaders,
  nameExists,
  resolveCategory,
  seed,
  store,
  toResponse,
  validate,
};
