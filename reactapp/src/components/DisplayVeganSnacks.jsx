import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useCatalogue } from '../context/SnackContext';
import useDebouncedValue from '../hooks/useDebouncedValue';
import {
  categoryLabel,
  formatCurrency,
  formatDate,
  formatExpiry,
  formatFreshness,
  formatPriceBand,
  formatQuantity,
  freshnessKey,
  priceBandKey,
  resolveExpiryDate,
} from '../utils/formatters';
import './DisplayVeganSnacks.css';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest' },
  { value: 'name', label: 'Name' },
  { value: 'price-low', label: 'Price low-high' },
  { value: 'price-high', label: 'Price high-low' },
  { value: 'expiry', label: 'Expiry' },
];

const SKELETON_CARDS = 6;

const toTimestamp = (value) => {
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 0 : parsed.getTime();
};

const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const expiryTimestamp = (snack) => {
  const expiry = resolveExpiryDate(snack);
  if (!expiry) return Number.MAX_SAFE_INTEGER;
  return toTimestamp(expiry);
};

const SORT_COMPARATORS = {
  newest: (a, b) => toTimestamp(b.createdAt) - toTimestamp(a.createdAt) || toNumber(b.id) - toNumber(a.id),
  name: (a, b) => String(a.snackName || '').localeCompare(String(b.snackName || '')),
  'price-low': (a, b) => toNumber(a.price) - toNumber(b.price),
  'price-high': (a, b) => toNumber(b.price) - toNumber(a.price),
  expiry: (a, b) => expiryTimestamp(a) - expiryTimestamp(b),
};

/**
 * Catalogue view listing every vegan snack with search, filtering and sorting.
 */
function DisplayVeganSnacks() {
  const { snacks, loading, error, refresh, stats } = useCatalogue();

  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [sortOrder, setSortOrder] = useState('newest');

  // Filtering runs against the debounced term to keep typing smooth.
  const debouncedSearch = useDebouncedValue(searchTerm, 250);

  const typeOptions = useMemo(() => {
    const labels = new Set();

    snacks.forEach((snack) => {
      const label = categoryLabel(snack.snackType || snack.category);
      if (label && label !== '—') labels.add(label);
    });

    return Array.from(labels).sort((a, b) => a.localeCompare(b));
  }, [snacks]);

  const visibleSnacks = useMemo(() => {
    const query = debouncedSearch.trim().toLowerCase();

    const filtered = snacks.filter((snack) => {
      const matchesSearch =
        !query ||
        [snack.snackName, snack.snackType, snack.category].some((field) =>
          String(field || '')
            .toLowerCase()
            .includes(query)
        );

      const matchesType =
        typeFilter === 'ALL' || categoryLabel(snack.snackType || snack.category) === typeFilter;

      return matchesSearch && matchesType;
    });

    const comparator = SORT_COMPARATORS[sortOrder] || SORT_COMPARATORS.newest;
    return [...filtered].sort(comparator);
  }, [snacks, debouncedSearch, typeFilter, sortOrder]);

  const hasFilters = searchTerm.trim() !== '' || typeFilter !== 'ALL';

  return (
    <section className="catalogue">
      <div className="container">
        <header className="catalogue__header">
          <div>
            <p className="section-heading__eyebrow">Catalogue</p>
            <h1 className="catalogue__title">Your Vegan Snack Shelf</h1>
            <p className="catalogue__lead">
              Every snack currently tracked, with freshness, shelf life and the value of
              your stock at a glance.
            </p>
          </div>
          <Link to="/apply" className="button button--primary">
            Add a new snack
          </Link>
        </header>

        <dl className="summary-strip">
          <div className="summary-strip__item">
            <dt>Total snacks</dt>
            <dd>{stats.totalCount}</dd>
          </div>
          <div className="summary-strip__item">
            <dt>Total quantity</dt>
            <dd>{formatQuantity(stats.totalQuantity)}</dd>
          </div>
          <div className="summary-strip__item summary-strip__item--accent">
            <dt>Total inventory value</dt>
            <dd>{formatCurrency(stats.totalValue)}</dd>
          </div>
        </dl>

        <div className="toolbar">
          <div className="toolbar__field toolbar__field--search">
            <label className="toolbar__label" htmlFor="snack-search">
              Search snacks
            </label>
            <input
              className="toolbar__input"
              id="snack-search"
              name="snack-search"
              type="search"
              placeholder="Search by name or type"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </div>

          <div className="toolbar__field">
            <label className="toolbar__label" htmlFor="snack-type-filter">
              Filter by type
            </label>
            <select
              className="toolbar__select"
              id="snack-type-filter"
              name="snack-type-filter"
              value={typeFilter}
              onChange={(event) => setTypeFilter(event.target.value)}
            >
              <option value="ALL">All types</option>
              {typeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>

          <div className="toolbar__field">
            <label className="toolbar__label" htmlFor="snack-sort">
              Sort by
            </label>
            <select
              className="toolbar__select"
              id="snack-sort"
              name="snack-sort"
              value={sortOrder}
              onChange={(event) => setSortOrder(event.target.value)}
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <button className="button button--ghost toolbar__refresh" type="button" onClick={refresh}>
            Refresh
          </button>
        </div>

        {loading && (
          <div className="snack-grid" aria-hidden="true">
            {Array.from({ length: SKELETON_CARDS }).map((_, index) => (
              <div className="snack-card snack-card--skeleton" key={index}>
                <div className="skeleton skeleton--title" />
                <div className="skeleton skeleton--subtitle" />
                <div className="skeleton skeleton--line" />
                <div className="skeleton skeleton--line" />
                <div className="skeleton skeleton--block" />
              </div>
            ))}
          </div>
        )}

        {!loading && error && (
          <div className="state-panel state-panel--error" role="alert">
            <div className="state-panel__illustration" aria-hidden="true">
              <span />
              <span />
              <span />
            </div>
            <h2 className="state-panel__title">The catalogue could not be loaded</h2>
            <p className="state-panel__message">{error}</p>
            <button className="button button--primary" type="button" onClick={refresh}>
              Retry
            </button>
          </div>
        )}

        {!loading && !error && snacks.length === 0 && (
          <div className="state-panel state-panel--empty">
            <div className="state-panel__illustration" aria-hidden="true">
              <span />
              <span />
              <span />
            </div>
            <h2 className="state-panel__title">No vegan snacks submitted yet.</h2>
            <p className="state-panel__message">
              The shelf is empty. Add your first vegan snack and it will appear here with
              its freshness badge and inventory value.
            </p>
            <Link to="/apply" className="button button--primary">
              Add your first snack
            </Link>
          </div>
        )}

        {!loading && !error && snacks.length > 0 && visibleSnacks.length === 0 && (
          <div className="state-panel state-panel--empty">
            <h2 className="state-panel__title">No snacks match your filters</h2>
            <p className="state-panel__message">
              Try a different search term or clear the type filter to see the full
              catalogue again.
            </p>
            <button
              className="button button--ghost"
              type="button"
              onClick={() => {
                setSearchTerm('');
                setTypeFilter('ALL');
              }}
            >
              Clear filters
            </button>
          </div>
        )}

        {!loading && !error && visibleSnacks.length > 0 && (
          <>
            <p className="catalogue__count" aria-live="polite">
              Showing {visibleSnacks.length} of {snacks.length} snacks
            </p>

            <ul className="snack-grid">
              {visibleSnacks.map((snack) => (
                <li className="snack-card" key={snack.id || snack.snackName}>
                  <div className="snack-card__head">
                    <div>
                      <h2 className="snack-card__name">{snack.snackName}</h2>
                      <p className="snack-card__type">
                        {snack.snackType || categoryLabel(snack.category)}
                      </p>
                    </div>
                    <span
                      className={`badge badge--freshness badge--${freshnessKey(snack.freshnessStatus)}`}
                    >
                      {formatFreshness(snack.freshnessStatus)}
                    </span>
                  </div>

                  <dl className="snack-card__details">
                    <div className="snack-card__detail">
                      <dt>Quantity</dt>
                      <dd>{formatQuantity(snack.quantity)}</dd>
                    </div>
                    <div className="snack-card__detail">
                      <dt>Price</dt>
                      <dd>{formatCurrency(snack.price)}</dd>
                    </div>
                    <div className="snack-card__detail">
                      <dt>Expiry (months)</dt>
                      <dd>
                        {snack.expiryInMonths === null || snack.expiryInMonths === undefined
                          ? '—'
                          : formatExpiry(snack.expiryInMonths)}
                      </dd>
                    </div>
                    <div className="snack-card__detail">
                      <dt>Expiry date</dt>
                      <dd>{formatDate(resolveExpiryDate(snack))}</dd>
                    </div>
                    <div className="snack-card__detail snack-card__detail--wide">
                      <dt>Total value</dt>
                      <dd>{formatCurrency(snack.totalValue)}</dd>
                    </div>
                  </dl>

                  <div className="snack-card__foot">
                    <span className={`badge badge--band badge--${priceBandKey(snack.priceBand)}`}>
                      {formatPriceBand(snack.priceBand)}
                    </span>
                    <span className="snack-card__category">
                      {categoryLabel(snack.category)}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}

        {hasFilters && !loading && !error && snacks.length > 0 && (
          <p className="catalogue__filters-note">Filters are applied to the list above.</p>
        )}
      </div>
    </section>
  );
}

export default DisplayVeganSnacks;
