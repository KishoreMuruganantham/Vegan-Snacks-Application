import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

/**
 * Site footer with the brand blurb, quick links, the technology stack and the
 * copyright notice.
 */
function Footer() {
  return (
    <footer className="site-footer">
      <div className="container site-footer__inner">
        <div className="site-footer__brand">
          <p className="site-footer__brand-name">Vegan Snacks</p>
          <p className="site-footer__blurb">
            A calm, plant-forward catalogue for the snacks you keep on hand — freshness,
            stock levels and value tracked in one place.
          </p>
        </div>

        <div className="site-footer__column">
          <h2 className="site-footer__heading">Quick links</h2>
          <ul className="site-footer__links">
            <li>
              <Link to="/">Home</Link>
            </li>
            <li>
              <Link to="/getAllVeganSnacks">Snacks Details</Link>
            </li>
            <li>
              <Link to="/apply">Add a Snack</Link>
            </li>
          </ul>
        </div>

        <div className="site-footer__column">
          <h2 className="site-footer__heading">Built with</h2>
          <p className="site-footer__stack">React 18 · Spring Boot 3 · H2 Database</p>
          <p className="site-footer__note">
            A case study application built with the React 18 component model and a
            RESTful Spring Boot service.
          </p>
        </div>
      </div>

      <div className="container site-footer__bottom">
        <p className="site-footer__copyright">
          © 2024 Vegan Snacks Application. All rights reserved.
        </p>
        <p className="site-footer__author">
          <span>© 2024</span>
          <span className="site-footer__separator" aria-hidden="true">
            ·
          </span>
          <span>Kishore Muruganantham</span>
        </p>
      </div>
    </footer>
  );
}

export default Footer;
