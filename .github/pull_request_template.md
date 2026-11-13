## What does this change?

<!-- One or two sentences. What is the change and why does it exist? -->

## Type of change

- [ ] Bug fix
- [ ] New feature
- [ ] Refactor / internal cleanup
- [ ] Documentation
- [ ] Build / CI / tooling

## Affected area

- [ ] `springapp/` — backend (controller / service / repository / model / utility)
- [ ] `reactapp/` — frontend (components / hooks / context / utils)
- [ ] Docs only

## How was this verified?

<!-- Paste the relevant test output or manual steps. -->

- [ ] `cd springapp && mvn test`
- [ ] `cd reactapp && CI=true npm test -- --watchAll=false`
- [ ] `cd reactapp && npm run build`
- [ ] Manually exercised the running application

## Checklist

- [ ] The change is covered by a test
- [ ] `CHANGELOG.md` updated under *Unreleased*
- [ ] No new dependencies without justification
- [ ] UI verified from 360px to 1440px and keyboard-only
- [ ] The API contract documented in `README.md` still matches the code
- [ ] No tooling or generator references in the source
