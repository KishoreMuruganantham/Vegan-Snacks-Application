# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.0] - 2026-11-13

The application is now hosted on Vercel and the repository carries the
screenshots of every page.

### Added

- **A live deployment** on the [Vercel Hobby plan](https://vercel.com/docs/plans/hobby)
  at <https://vegan-snacks-application-virid.vercel.app>. The plan is free,
  has no billing cycle and no card on file.
- A serverless API in `reactapp/api` that implements exactly the contract of
  the Spring Boot API — the same paths, the same validation messages, the
  same category resolution and the same derived pricing, freshness and
  valuation figures. It keeps the catalogue in memory, so the deployment
  needs no external database.
- `reactapp/vercel.json`, which configures the build, the output directory,
  the client side routes and the build time environment of the deployment.
- `reactapp/scripts/test-api.js`, a harness that runs the serverless
  functions locally so their behaviour can be verified without a deployment.
- Screenshots of every page of the application in `docs/screenshots`, used by
  the README.
- The Spring Boot API publishes the two endpoints of the problem statement
  below `/api` as well, so the same client reaches either backend without a
  rebuild.

### Changed

- `reactapp/src/apiConfig.js` resolves the base URL of the backend from the
  build time configuration, from the hostname of the page or from the
  `REACT_APP_API_BASE_URL` variable, in that order. The client therefore
  points at the Spring Boot API during local development and at the
  serverless API of the deployment everywhere else.
- The settings of the development server moved from `reactapp/.env` to
  `reactapp/.env.development`, so they no longer leak into the production
  build.
- The README carries the live demo, the screenshots and a section that
  explains the architecture of the deployment and its cost.

---

## [1.1.1] - 2026-11-13

### Changed

- The application header now floats above the page and adapts to whatever
  sits behind it:
  - on the landing page it is **fully transparent**, so the deep green hero
    artwork is visible straight through it;
  - once the page is scrolled it takes on a translucent green surface so the
    navigation keeps its contrast over the artwork;
  - on every other route it keeps the light surface of the application.
- The navigation colours are driven by custom properties per surface, so the
  light type of the dark hero and the dark type of the light pages share a
  single set of rules.
- The call to action switches to the lime accent while it sits on the dark
  hero, which keeps it separated from the artwork.

### Added

- The header measures itself with a `ResizeObserver` and publishes its height
  as the `--header-h` custom property. The page offset therefore stays exact
  on every viewport, including the narrow layouts where the navigation wraps
  onto a second row.
- A `--header-h` design token with a fallback value for the first paint.

### Fixed

- The three floating cards of the hero overlapped each other on wide
  viewports, which clipped the text of the middle card. The column now
  reserves enough height and the cards are spread over it with explicit
  stacking order.

---

## [1.1.0] - 2026-11-13

The full engineering release. The functional skeleton shipped in `1.0.0` is
rebuilt on a layered Spring Boot core and a redesigned React client, with the
complete problem statement implemented end to end.

### Added — Backend (`springapp`)

- Layered package structure `com.examly.springapp` with `controller`,
  `service`, `repository`, `model`, `mapper`, `dto.request`, `dto.response`,
  `exception`, `configuration` and `utility` packages.
- `VeganSnack` JPA entity persisted to the `vegan_snacks` table with a
  `BigDecimal` price (`precision=12, scale=2`), an enum-backed `category` and
  an audit column `created_at`.
- `SnackCategory` enum with keyword-driven resolution from the free-text snack
  type (`NUTS_AND_SEEDS`, `CHIPS_AND_CRUNCHIES`, `FRUIT_SNACKS`, `CHOCOLATE`,
  `ENERGY_BARS`, `BAKED_TREATS`, `OTHERS`).
- `FreshnessStatus` enum (`EXPIRED`, `EXPIRING_SOON`, `FRESH`, `SHELF_STABLE`)
  derived from the shelf life, together with a computed expiry date.
- `SnackPricingPolicy` utility — pure, unit-testable pricing bands
  (`BUDGET`, `STANDARD`, `PREMIUM`, `ARTISAN`), inventory valuation and
  freshness computation.
- `SnackValidator` utility — hand-written field validation that reports **all**
  offending fields at once instead of failing on the first one.
- `VeganSnackService` / `SnackCatalogService` — orchestration layer covering
  normalisation, duplicate detection, persistence, search, filtering, sorting
  and catalog statistics.
- `VeganSnackMapper` — entity ↔ request/response DTO conversion, keeping
  presentation concerns out of the entity.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) translating domain
  exceptions into a consistent `ApiErrorResponse` envelope with per-field
  error messages and no leaked stack traces.
- `CorsConfiguration` — CORS for `http://localhost:8081` and
  `http://127.0.0.1:8081` across every endpoint and verb.
- `DataSeeder` — idempotent seeding of six catalogue entries on first boot.
- New catalog REST surface under `/api/v1/snacks`: `GET` list with `search`
  and `type` filters, `GET` by id, `PUT`, `DELETE`, `GET /stats` and
  `GET /health`.
- JUnit 5 test suite covering the validator, the pricing policy, category
  mapping, duplicate detection, statistics and the controller contract via
  MockMvc.

### Added — Frontend (`reactapp`)

- Complete `src/components` set with a dedicated stylesheet per component:
  `NavBar`, `Home`, `ApplyForm`, `DisplayVeganSnacks` and `Footer`.
- `SnackContext` — application-wide catalogue state, derived statistics and a
  global toast channel that survives route changes.
- `useSnacks`, `useSnackForm` and `useDebouncedValue` hooks that isolate data
  fetching, form state and search debouncing from the components.
- Pure `validators` and `formatters` utilities, unit-testable in isolation.
- `DisplayVeganSnacks` rebuilt as a responsive card grid with skeleton
  loading, an empty state, an error state with retry, debounced search, type
  filtering, multi-key sorting and a live summary strip.
- `ApplyForm` rebuilt with inline field validation, character counters, a live
  preview card, a disabled-while-submitting button and a global success toast.
- `Home` rebuilt as a full landing page: hero, feature grid, "how it works"
  strip and live catalogue statistics.
- `Footer` rebuilt as a multi-column footer with quick links and a technology
  line.
- A single design-token layer in `src/index.css` (colour, radius, shadow,
  typography) consumed by every component stylesheet.
- Accessibility pass — semantic landmarks, `aria-current` navigation state,
  `aria-invalid` / `aria-describedby` on invalid fields, `role="alert"` on
  error banners, visible focus rings and reduced-motion guards.
- Responsive pass verified from 360px to 1440px with no horizontal overflow.
- `src/tests/App.test.js` — six rendering, routing, validation and submission
  tests.

### Added — Repository

- Modern `README.md` with Mermaid architecture, sequence, class and ER
  diagrams, a complete API reference and run instructions.
- `CHANGELOG.md` (this file) and an MIT `LICENSE`.
- `.gitignore` covering Maven, Node, IDE and OS artefacts.
- Maven wrapper (`mvnw` / `mvnw.cmd`) for machines without a global Maven
  installation.
- GitHub repository metadata, topics and a tagged release.

### Changed

- Price is now a `BigDecimal` with two decimal places instead of an integer,
  so fractional pricing survives a round trip.
- Quantity is now a strongly typed `Integer` instead of a `String`.
- `GET /getAllVeganSnacks` now returns newest-first and never `null`.
- `POST /addVeganSnack` returns `400` with per-field errors instead of a
  plain-text body, and `409` on a duplicate snack name.
- The dev server port is configured through `reactapp/.env` instead of a
  shell-specific `PORT=8081` prefix, which keeps `npm start` portable across
  Windows, macOS and Linux.
- The footer copyright now uses the real `©` character (it previously
  rendered as `Ac`).

### Fixed

- Footer copyright symbol rendered as `Ac` instead of `©`.
- `npm start` was not runnable on Windows because the port was set with a
  POSIX-only environment prefix.
- The response body of a failed submission was a raw string, so the client
  could not map server-side field errors back onto the form.

---

## [1.0.0] - 2026-11-13

Initial working submission covering the core problem statement.

- React client with `Home`, `ApplyForm`, `DisplayVeganSnacks`, `NavBar` and
  `Footer` components and the three mandated routes.
- Spring Boot API exposing `POST /addVeganSnack` and
  `GET /getAllVeganSnacks` on port 8080 with an H2 in-memory database.
- Client-side validation with the mandated error messages.
