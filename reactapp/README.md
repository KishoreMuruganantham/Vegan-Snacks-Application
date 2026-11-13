# Vegan Snacks Application — Frontend

React 18 single page application for the **Vegan Snacks Application** case study. It talks to the
Spring Boot backend over plain `fetch()` calls and presents a landing page, a snack catalogue with
search, filtering and sorting, and a validated "add a snack" form.

## Stack

| Concern            | Choice                                                        |
| ------------------ | -------------------------------------------------------------- |
| UI runtime         | React 18 (function components and hooks only)                  |
| Routing            | `react-router-dom` v6 (`BrowserRouter` lives inside `App.js`)  |
| Styling            | Plain CSS, one stylesheet per component, shared design tokens   |
| HTTP               | Native `fetch()` with `AbortController` based request handling |
| Tests              | Jest, React Testing Library v13, `user-event` v14               |

No component library, CSS framework or HTTP client is used.

## Getting started

```bash
npm install     # dependencies
npm start       # development server on http://localhost:8081
npm run build   # production bundle in build/
npm test        # watch mode
```

CI style run:

```bash
CI=true npm test -- --watchAll=false
```

The development server is configured through `.env`:

```
PORT=8081
HOST=0.0.0.0
BROWSER=none
```

## Backend configuration

All endpoint URLs are derived from a single base URL in `src/apiConfig.js`:

```js
export const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  GET_ALL_SNACKS: '/getAllVeganSnacks',
  ADD_SNACK: '/addVeganSnack',
  STATS: '/api/v1/snacks/stats',
  HEALTH: '/api/v1/health',
};
```

Set `REACT_APP_API_BASE_URL` to point the build at another environment.

### Endpoint contract

| Method | Path                    | Body / response                                                     |
| ------ | ----------------------- | ------------------------------------------------------------------- |
| `GET`  | `/getAllVeganSnacks`    | `200` with a JSON array of snack objects                            |
| `POST` | `/addVeganSnack`        | `{ snackName, snackType, quantity, price, expiryInMonths }` → `200` |

A snack object looks like this:

```json
{
  "id": 1,
  "snackName": "Roasted Almonds",
  "snackType": "Nuts and Seeds",
  "category": "NUTS_AND_SEEDS",
  "quantity": 25,
  "price": 149.5,
  "expiryInMonths": 6,
  "expiryDate": "2027-05-13",
  "freshnessStatus": "FRESH",
  "priceBand": "STANDARD",
  "totalValue": 3737.5,
  "isVegan": true,
  "createdAt": "2026-11-13T05:20:31.123"
}
```

Validation failures are surfaced as `400` or `409` responses:

```json
{
  "timestamp": "2026-11-13T05:20:31.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Snack details are invalid.",
  "fieldErrors": { "price": "Price must be a positive number" }
}
```

`fieldErrors` are mapped back onto the individual form inputs, while the message is shown in an
inline error banner.

## Project structure

```
src/
  apiConfig.js              base URL and endpoint map
  App.js                    router, layout route and toast region
  assets/                   logo and leaf pattern artwork
  components/               NavBar, Home, ApplyForm, DisplayVeganSnacks, Footer
  context/SnackContext.jsx  shared catalogue state, stats and notifications
  hooks/                    useSnacks, useSnackForm, useDebouncedValue
  utils/                    formatters and pure validation rules
  tests/App.test.js         integration tests for the whole application
```

### Routes

| Path                  | Screen                 |
| --------------------- | ---------------------- |
| `/`                   | Landing page           |
| `/apply`              | Add a vegan snack form |
| `/getAllVeganSnacks`  | Snack catalogue        |
| `*`                   | Not found page         |

The navigation bar is rendered by the layout route and therefore stays mounted on every page. The
footer and the notification area are rendered once, outside the routed outlet.

### Design tokens

Colours, radii, shadows and font stacks are declared as CSS custom properties in `src/index.css` and
reused by every component stylesheet. `Inter` and `Outfit` are loaded from Google Fonts with a local
system fallback stack, so the interface stays legible without network access.

## Author

Kishore Muruganantham — kishore.muruganantham@gmail.com
