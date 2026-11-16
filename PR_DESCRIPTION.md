# Pull Request Description

## Summary
This change implements a client-only login and password recovery interface with shared validation and browser-level testing. It keeps the behavior within the approved static app scope and avoids any backend or authentication feature.

## Changes made
- Created `client/index.html`, `client/styles.css`, `client/validation.js`, and `client/login.js`.
- Added browser validation and a recovery flow that reuses the login username rules.
- Added unit and Playwright tests plus coverage and report scripts.

## Test evidence
- `npm run test:unit`
- `npm test`
- `npm run verify`

## Known limitations
- The app is intentionally static and does not authenticate or reset passwords.
- The browser test relies on local file loading and requires Playwright Chromium.

## Reviewer checklist
- [ ] The app remains browser-only and does not add a backend.
- [ ] Validation rules match the requirements.
- [ ] Recovery flow is limited to validation feedback.
- [ ] Tests cover the login and recovery flows.
