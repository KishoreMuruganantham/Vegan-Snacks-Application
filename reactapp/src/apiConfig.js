/**
 * Central configuration for every backend call made by the application.
 *
 * The base URL is resolved once, in the following order:
 *
 * 1. `REACT_APP_API_BASE_URL` when it is set at build time. An empty value
 *    turns every endpoint into a relative request against the origin of the
 *    page, which is what a hosted deployment uses.
 * 2. `http://localhost:8080` when the client runs on a development machine.
 * 3. The origin of the page everywhere else, which is the case for a hosted
 *    deployment that serves its API from the same domain.
 *
 * The endpoints live below `/api` so that they never collide with the routes
 * of the single page application. The Spring Boot API exposes them on exactly
 * these paths, and the hosting platform maps them onto the same paths.
 */

/** Base URL of the Spring Boot API used during local development. */
const DEFAULT_BASE_URL = 'http://localhost:8080';

/**
 * Resolves the base URL of the backend.
 *
 * @returns {string} the base URL without a trailing slash
 */
const readBaseUrl = () => {
  const configured = process.env.REACT_APP_API_BASE_URL;
  if (configured !== undefined) {
    return configured;
  }

  if (typeof window === 'undefined' || !window.location) {
    return DEFAULT_BASE_URL;
  }

  const hostname = window.location.hostname;
  const runsLocally = hostname === 'localhost' || hostname === '127.0.0.1';

  return runsLocally ? DEFAULT_BASE_URL : '';
};

/** Base URL every request is built on top of. */
export const API_BASE_URL = readBaseUrl().replace(/\/+$/, '');

/** Paths of the backend endpoints used by the application. */
export const API_ENDPOINTS = {
  GET_ALL_SNACKS: '/api/getAllVeganSnacks',
  ADD_SNACK: '/api/addVeganSnack',
  STATS: '/api/v1/snacks/stats',
  HEALTH: '/api/v1/health',
};

/**
 * Builds an absolute endpoint URL for the configured backend.
 * Keeping this in one place avoids string concatenation drifting across files.
 *
 * @param {string} endpoint path of the endpoint
 * @returns {string} the URL to request
 */
export const buildApiUrl = (endpoint) => `${API_BASE_URL}${endpoint}`;

export default API_BASE_URL;
