# Architecture

## Overview
This project is a client-only experience that runs in the browser without a backend. It keeps validation logic shared in `client/validation.js` and page behavior in `client/login.js`.

## Components

- `client/index.html`: markup for the login and recovery views.
- `client/styles.css`: layout, focus treatment, and visual feedback.
- `client/validation.js`: shared username/password validators and browser compatibility hooks.
- `client/login.js`: initialization, form handling, and view switching.
- `tests/unit/login.test.js`: Node-based validation and page initialization checks.
- `tests/playwright/login.spec.js`: browser-level regression checks.

## Data flow
1. The page loads from a local file.
2. `login.js` initializes the form controls.
3. Shared validators check both username and password rules.
4. Accessible status messages display validation results.
5. Recovery flow reuses the same username validation without introducing reset logic.

## Design constraints
- No framework or backend.
- No network dependency.
- Validation only; no authentication or persistence.
