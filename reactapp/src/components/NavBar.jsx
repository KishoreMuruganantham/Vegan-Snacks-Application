import React from 'react';
import { Link, NavLink } from 'react-router-dom';
import './NavBar.css';

/**
 * Menu links keep the `nav-link` class at all times. The active route is
 * marked by NavLink itself, which appends the `active` class and maintains the
 * `aria-current` attribute, so assistive technology and the stylesheet stay in
 * sync.
 */

/** Inline brand mark - avoids an extra network request for the logo. */
const BrandMark = () => (
  <svg
    className="brand__mark"
    viewBox="0 0 48 48"
    role="img"
    aria-label="Vegan snacks leaf mark"
    focusable="false"
  >
    <defs>
      <linearGradient id="brandMarkGradient" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stopColor="#8ADCA1" />
        <stop offset="100%" stopColor="#C3F05C" />
      </linearGradient>
    </defs>
    <circle cx="24" cy="24" r="23" fill="#0B2E1F" />
    <path
      d="M31.5 12.5c1.2 9.6-3.3 17.3-11.6 18.9-3.1.6-5.6-.2-7.6-1.9 5.8-1 9.4-4.3 10.9-9.8-3.4 2.3-6.9 3.2-10.7 2.7 2.6-4.9 8.4-9.4 19-9.9z"
      fill="url(#brandMarkGradient)"
    />
    <path
      d="M13.4 34.6c3.9-6.6 9.4-11.4 16.6-14.5"
      fill="none"
      stroke="#0B2E1F"
      strokeWidth="1.6"
      strokeLinecap="round"
      opacity=".55"
    />
  </svg>
);

/**
 * Sticky application header with the brand mark and the primary navigation.
 */
function NavBar() {
  return (
    <header className="site-header">
      <nav className="site-nav" aria-label="Primary">
        <div className="site-nav__inner">
          <Link to="/" className="brand">
            <BrandMark />
            <span className="brand__text">Vegan Snacks Application</span>
          </Link>

          <ul className="nav-list">
            <li className="nav-list__item">
              <NavLink to="/" className="nav-link">
                Home
              </NavLink>
            </li>
            <li className="nav-list__item">
              <NavLink to="/getAllVeganSnacks" className="nav-link">
                Snacks Details
              </NavLink>
            </li>
            <li className="nav-list__item nav-list__item--cta">
              <NavLink to="/apply" className="nav-link">
                Add a Snack
              </NavLink>
            </li>
          </ul>
        </div>
      </nav>
    </header>
  );
}

export default NavBar;
