# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
