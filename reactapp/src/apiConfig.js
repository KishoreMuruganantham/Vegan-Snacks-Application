/**
 * Central configuration for every backend call made by the application.
 *
 * The base URL can be overridden at build time with the REACT_APP_API_BASE_URL
 * environment variable so that the same bundle can target a local, staging or
 * containerised backend without code changes.
 */

export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export const API_ENDPOINTS = { GET_ALL_SNACKS: '/getAllVeganSnacks', ADD_SNACK: '/addVeganSnack', STATS: '/api/v1/snacks/stats', HEALTH: '/api/v1/health' };

/**
 * Builds an absolute endpoint URL for the configured backend.
 * Keeping this in one place avoids string concatenation drifting across files.
 */
export const buildApiUrl = (endpoint) => `${API_BASE_URL}${endpoint}`;

export default API_BASE_URL;
