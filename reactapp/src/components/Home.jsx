import React from 'react';
import { Link } from 'react-router-dom';
import { useCatalogue } from '../context/SnackContext';
import { formatCurrency } from '../utils/formatters';
import './Home.css';

/** Decorative leaf used as a soft background motif inside the hero. */
const Leaf = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 220 220"
    aria-hidden="true"
    focusable="false"
  >
    <path
      d="M110 8c58 40 84 106 36 178-12 18-30 26-46 26 22-38 30-80 22-126-22 30-52 48-90 54 6-50 32-98 78-132z"
      fill="currentColor"
    />
    <path
      d="M110 20v180"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      opacity=".45"
    />
  </svg>
);

const LeafIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path
      d="M20 4C10 4 4 9 4 15.5c0 2.6 1.3 4.5 3.2 5.6C8.5 14 12 11.5 16 10.8c-3.6 1.6-6.2 4.3-7.9 8.1 1.3.7 2.7 1.1 4.4 1.1C18.6 20 22 11 20 4z"
      fill="currentColor"
    />
  </svg>
);

const GaugeIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path
      d="M12 3a9 9 0 100 18 9 9 0 000-18zm0 3.2a5.8 5.8 0 110 11.6 5.8 5.8 0 010-11.6zm-.9 2.4v4.2l3 1.8.9-1.5-2.1-1.2V8.6h-1.8z"
      fill="currentColor"
    />
  </svg>
);

const CoinsIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path
      d="M12 2.8c-4.7 0-8.5 2.3-8.5 5.2v8c0 2.9 3.8 5.2 8.5 5.2s8.5-2.3 8.5-5.2V8c0-2.9-3.8-5.2-8.5-5.2zm0 1.9c3.8 0 6.6 1.7 6.6 3.3S15.8 11.3 12 11.3 5.4 9.6 5.4 8s2.8-3.3 6.6-3.3zM5.4 10.2c1.6 1.4 4 2.2 6.6 2.2s5-.8 6.6-2.2v2.9c0 1.6-2.8 3.3-6.6 3.3s-6.6-1.7-6.6-3.3v-2.9zm0 5.1c1.6 1.4 4 2.2 6.6 2.2s5-.8 6.6-2.2v2.6c0 1.6-2.8 3.3-6.6 3.3s-6.6-1.7-6.6-3.3v-2.6z"
      fill="currentColor"
    />
  </svg>
);

const BoltIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path
      d="M13.2 2L5 13.4h5.4L9.8 22l8.2-11.4h-5.4L13.2 2z"
      fill="currentColor"
    />
  </svg>
);

const FEATURES = [
  {
    title: 'Plant-based by default',
    description:
      'Every record carries a vegan flag and a clear category, so the catalogue stays honest from the first entry.',
    Icon: LeafIcon,
  },
  {
    title: 'Freshness in plain sight',
    description:
      'Shelf life is converted into a freshness badge and an expiry date, so you know what to reach for first.',
    Icon: GaugeIcon,
  },
  {
    title: 'Value you can verify',
    description:
      'Quantity and price are combined into a running inventory value that matches the figures you paid for.',
    Icon: CoinsIcon,
  },
  {
    title: 'Built for quick updates',
    description:
      'Add a snack in seconds, then filter, sort and search the shelf without waiting for a page reload.',
    Icon: BoltIcon,
  },
];

const STEPS = [
  {
    title: 'Add your snack',
    description: 'Capture the name, type, quantity, price and shelf life of a new vegan snack.',
  },
  {
    title: 'Review the shelf',
    description: 'Browse a responsive card view with freshness badges and live inventory totals.',
  },
  {
    title: 'Keep it fresh',
    description: 'Spot items that are close to expiry and restock before the shelf runs dry.',
  },
];

const HERO_POINTS = [
  { title: 'Live freshness tracking', description: 'Expiry badges that update with the shelf.' },
  { title: 'Stock value at a glance', description: 'Quantity and price rolled into one total.' },
  { title: 'One tap to add a snack', description: 'A short form that saves straight to the catalogue.' },
];

const HERO_CARD_POSITIONS = ['primary', 'secondary', 'tertiary'];

/**
 * Landing page for the application.
 */
function Home() {
  const { stats, loading, error } = useCatalogue();

  const hasLiveData = !error && !loading && stats.totalCount > 0;
  const showUnavailable = Boolean(error);

  return (
    <div className="home">
      <section className="hero" aria-labelledby="hero-title">
        <Leaf className="hero__leaf hero__leaf--one" />
        <Leaf className="hero__leaf hero__leaf--two" />
        <Leaf className="hero__leaf hero__leaf--three" />

        <div className="container hero__inner">
          <div className="hero__content">
            <p className="hero__eyebrow">
              <span className="hero__eyebrow-dot" aria-hidden="true" />
              Vegan snacks, thoughtfully catalogued
            </p>

            <h1 className="hero__title" id="hero-title">
              Welcome to the Vegan Snacks App!
            </h1>

            <p className="hero__lead">
              Explore a delightful range of vegan snacks! Enjoy tasty, guilt-free treats made from natural ingredients.
            </p>

            <div className="hero__actions">
              <Link to="/apply" className="button button--primary button--large">
                Add Your Vegan Snack
              </Link>
              <Link to="/getAllVeganSnacks" className="button button--ghost button--large">
                Browse Snacks
              </Link>
            </div>

            <ul className="hero__meta">
              <li>
                <strong>No sign-up</strong>
                <span>Open the shelf and start adding</span>
              </li>
              <li>
                <strong>Always current</strong>
                <span>Freshness recalculated on every load</span>
              </li>
              <li>
                <strong>Instant feedback</strong>
                <span>Validation as you type</span>
              </li>
            </ul>
          </div>

          <div className="hero__visual" aria-hidden="true">
            {HERO_POINTS.map((point, index) => (
              <div
                className={`hero-card hero-card--${HERO_CARD_POSITIONS[index]}`}
                key={point.title}
              >
                <p className="hero-card__label">{point.title}</p>
                <p className="hero-card__value">{point.description}</p>
                {index === 0 && (
                  <div className="hero-card__bar">
                    <span />
                    <span />
                    <span />
                    <span />
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="features" aria-labelledby="features-title">
        <div className="container">
          <div className="section-heading">
            <p className="section-heading__eyebrow">Why it works</p>
            <h2 className="section-heading__title" id="features-title">
              Everything a snack shelf needs, nothing it does not
            </h2>
            <p className="section-heading__lead">
              A focused toolkit for keeping a vegan snack catalogue accurate, readable
              and ready for the next restock.
            </p>
          </div>

          <ul className="feature-grid">
            {FEATURES.map((feature) => (
              <li className="feature-card" key={feature.title}>
                <span className="feature-card__icon" aria-hidden="true">
                  <feature.Icon />
                </span>
                <h3 className="feature-card__title">{feature.title}</h3>
                <p className="feature-card__description">{feature.description}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="steps" aria-labelledby="steps-title">
        <div className="container">
          <div className="section-heading section-heading--centered">
            <p className="section-heading__eyebrow">How it works</p>
            <h2 className="section-heading__title" id="steps-title">
              Three steps from snack to shelf
            </h2>
          </div>

          <ol className="steps__list">
            {STEPS.map((step, index) => (
              <li className="step" key={step.title}>
                <span className="step__number" aria-hidden="true">
                  {index + 1}
                </span>
                <h3 className="step__title">{step.title}</h3>
                <p className="step__description">{step.description}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="stats" aria-labelledby="stats-title">
        <div className="container">
          <div className="stats__head">
            <div>
              <p className="section-heading__eyebrow">Live catalogue</p>
              <h2 className="section-heading__title" id="stats-title">
                Your pantry in numbers
              </h2>
            </div>
            <Link to="/getAllVeganSnacks" className="stats__link">
              See every snack
              <span aria-hidden="true"> →</span>
            </Link>
          </div>

          <div className="stats__grid">
            <div className="stat-tile">
              <p className="stat-tile__label">Snacks catalogued</p>
              <p className="stat-tile__value">
                {showUnavailable ? '—' : stats.totalCount}
              </p>
              <p className="stat-tile__hint">
                {showUnavailable ? 'Live data unavailable' : 'Across every category'}
              </p>
            </div>

            <div className="stat-tile">
              <p className="stat-tile__label">Units in stock</p>
              <p className="stat-tile__value">
                {showUnavailable ? '—' : stats.totalQuantity}
              </p>
              <p className="stat-tile__hint">
                {showUnavailable ? 'Live data unavailable' : 'Total quantity on hand'}
              </p>
            </div>

            <div className="stat-tile stat-tile--accent">
              <p className="stat-tile__label">Inventory value</p>
              <p className="stat-tile__value">
                {showUnavailable ? '—' : formatCurrency(stats.totalValue)}
              </p>
              <p className="stat-tile__hint">
                {hasLiveData ? 'Quantity multiplied by unit price' : 'Waiting for catalogue data'}
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="cta-band" aria-labelledby="cta-title">
        <div className="container cta-band__inner">
          <div>
            <h2 className="cta-band__title" id="cta-title">
              Ready to fill the shelf?
            </h2>
            <p className="cta-band__lead">
              Add your first vegan snack and watch the catalogue, the freshness badges and
              the inventory totals come to life.
            </p>
          </div>
          <div className="cta-band__actions">
            <Link to="/apply" className="button button--primary button--large">
              Add your first snack
            </Link>
            <Link to="/getAllVeganSnacks" className="button button--ghost button--large">
              Open the catalogue
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;
