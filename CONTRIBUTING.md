# Contributing to the Vegan Snacks Application

Thanks for taking the time to contribute. This project is the reference
implementation of the **Vegan Snacks** case study — a React 18 client talking
to a Spring Boot 3 REST API.

---

## Getting set up

```bash
git clone https://github.com/KishoreMuruganantham/Vegan-Snacks-Application.git
cd Vegan-Snacks-Application

# backend
cd springapp && mvn spring-boot:run

# frontend (second terminal)
cd reactapp && npm install && npm start
```

You need JDK 17+, Node 16+ and Maven 3.8+ (or use the bundled `./mvnw`).

---

## Ground rules

### 1. Respect the layering

```text
controller  ->  service  ->  repository  ->  entity
     |             |             |
   DTOs        policies      JPA entity
```

- **Controllers** translate HTTP into domain calls. No business rules.
- **Services** orchestrate. They call the validator, the pricing policy and
  the mapper, then the repository.
- **Utilities** (`SnackValidator`, `SnackPricingPolicy`) are pure functions —
  no Spring, no I/O. That is what makes them cheap to unit test.
- **Entities** never leak into the response body; always go through the
  mapper and a response DTO.

### 2. Validate on both sides

Client-side validation is a convenience; the API is the source of truth.
Any new field needs a rule in `SnackValidator`, a rule in
`src/utils/validators.js` and a test on both sides.

### 3. Reuse the design tokens

Never hard-code a colour, radius or shadow in a component stylesheet. Add a
token to `reactapp/src/index.css` and consume the variable.

### 4. Naming conventions

| Artefact | Convention | Example |
|----------|-----------|---------|
| Java package | lower-case, layered | `com.examly.springapp.service` |
| Java class | PascalCase | `VeganSnackService` |
| Java method / field | camelCase | `getAllVeganSnacks` |
| React component file | PascalCase | `DisplayVeganSnacks.jsx` |
| CSS file | matches its component | `DisplayVeganSnacks.css` |
| Hook | `use` prefix | `useSnacks` |
| Utility | descriptive camelCase | `formatCurrency` |

### 5. No stray tooling references

Comments and documentation are written in plain engineering voice. Do not
reference tooling, generators or assistants in the source.

---

## Before you open a pull request

```bash
cd springapp && mvn test          # backend suite
cd reactapp && CI=true npm test -- --watchAll=false   # frontend suite
cd reactapp && npm run build      # must compile with zero lint errors
```

Then:

- [ ] The change is covered by a test.
- [ ] `CHANGELOG.md` is updated under *Unreleased*.
- [ ] The UI still works from 360px to 1440px, keyboard-only.
- [ ] The public API contract in `README.md` still matches the code.

---

## Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat(backend): add duplicate snack name detection
fix(frontend): render the footer copyright symbol correctly
docs(readme): document the catalogue endpoints
```

---

## Reporting bugs

Open an issue with the **Bug report** template and include:

- What you expected to happen
- What actually happened
- Steps to reproduce
- Browser / JDK / Node versions
- The relevant console or server log excerpt

---

## Code of conduct

Be respectful and constructive. This is a learning project — every review is
aimed at making the code clearer, not proving who was right.
