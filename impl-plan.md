# Implementation Plan

## Phase 1: Setup
- Create the client directory and the static HTML, CSS, and JavaScript entry points.
- Define validation rules and shared logic.

## Phase 2: Login flow
- Wire the login form to shared validation.
- Show clear error feedback without leaking password content.
- Clear stale success state when the user edits the form.

## Phase 3: Recovery flow
- Reveal the recovery view from the login screen.
- Validate the username against the same rules.
- Keep the flow limited to validation and feedback.

## Phase 4: Verification
- Run Node unit tests.
- Run Playwright browser regression tests.
- Generate a coverage report and verify the results.
