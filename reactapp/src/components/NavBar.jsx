import React, { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
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
 *
 * The header floats above the page and adapts to what sits behind it:
 *
 * - `overlay` - on the landing page the hero artwork reaches the top of the
 *   viewport, so the header stays fully transparent and the deep green hero
 *   is visible through it.
 * - `deep` - once the page is scrolled the header gains a translucent green
 *   surface so the navigation keeps its contrast over the artwork.
 * - `solid` - on every other route the header uses the light surface of the
 *   application.
 */
function NavBar() {
  const location = useLocation();
  const headerRef = useRef(null);
  const [scrolled, setScrolled] = useState(false);

  /*
   * The header is taken out of the flow, so the page has to be offset by its
   * exact height. Measuring it here and publishing the result as a custom
   * property keeps that offset correct on every viewport, including the
   * narrow layouts where the navigation wraps onto a second row.
   */
  useEffect(() => {
    const node = headerRef.current;
    if (!node) {
      return undefined;
    }

    const publishHeight = () => {
      document.documentElement.style.setProperty('--header-h', `${node.offsetHeight}px`);
    };

    publishHeight();

    if (typeof ResizeObserver === 'undefined') {
      window.addEventListener('resize', publishHeight);
      return () => window.removeEventListener('resize', publishHeight);
    }

    const observer = new ResizeObserver(publishHeight);
    observer.observe(node);
    return () => observer.disconnect();
  }, []);

  /*
   * Track the scroll offset. The listener is re-attached on every route change
   * so the state always reflects the position of the page that is shown.
   */
  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 16);

    handleScroll();
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [location.pathname]);

  const overHero = location.pathname === '/';
  const variant = overHero ? (scrolled ? 'site-header--deep' : 'site-header--overlay') : 'site-header--solid';

  return (
    <header className={`site-header ${variant}`} ref={headerRef}>
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
