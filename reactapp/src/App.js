import React from 'react';
import { BrowserRouter, Link, Outlet, Route, Routes } from 'react-router-dom';
import NavBar from './components/NavBar';
import Home from './components/Home';
import ApplyForm from './components/ApplyForm';
import DisplayVeganSnacks from './components/DisplayVeganSnacks';
import Footer from './components/Footer';
import { SnackProvider, useSnackContext } from './context/SnackContext';
import './App.css';

/**
 * Layout route: the header and the primary landmark stay mounted while the
 * routed page changes.
 */
function Layout() {
  return (
    <div className="app-layout">
      <NavBar />
      <main className="app-main" id="main-content">
        <Outlet />
      </main>
    </div>
  );
}

/**
 * Catch-all route for unknown URLs.
 */
function NotFound() {
  return (
    <section className="not-found">
      <p className="not-found__code">404</p>
      <h1 className="not-found__title">We could not find that page</h1>
      <p className="not-found__lead">
        The page you are looking for does not exist. Head back to the shelf and carry on
        browsing.
      </p>
      <Link to="/" className="button button--primary">
        Return to the home page
      </Link>
    </section>
  );
}

/**
 * Global notification surface. It is rendered outside the routed outlet so a
 * message survives the navigation that follows a successful submission.
 */
function ToastRegion() {
  const { toast, dismissToast } = useSnackContext();

  if (!toast) return null;

  return (
    <div className="toast-region" role="status" aria-live="polite">
      <div className={`toast toast--${toast.type}`}>
        <div className="toast__body">
          {toast.title && <p className="toast__title">{toast.title}</p>}
          {toast.message && <p className="toast__message">{toast.message}</p>}
        </div>
        <button
          type="button"
          className="toast__dismiss"
          onClick={dismissToast}
          aria-label="Dismiss notification"
        >
          <span aria-hidden="true">×</span>
        </button>
      </div>
    </div>
  );
}

/**
 * Application root. The router lives inside the component so that the app can
 * be rendered as a self contained unit in tests.
 */
function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <SnackProvider>
        <div className="app-shell">
          <a className="skip-link" href="#main-content">
            Skip to main content
          </a>

          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<Home />} />
              <Route path="apply" element={<ApplyForm />} />
              <Route path="getAllVeganSnacks" element={<DisplayVeganSnacks />} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>

          <Footer />
          <ToastRegion />
        </div>
      </SnackProvider>
    </BrowserRouter>
  );
}

export default App;
