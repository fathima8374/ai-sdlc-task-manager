# Requirements

## Goal
Build a static, browser-only login and password recovery interface. The application must validate input formats and display accessible feedback without authenticating users or resetting passwords.

## Functional requirements

- FR-01: The app loads from `client/index.html` by opening the page directly in a browser.
- FR-02: The page includes labeled Username and Password fields, a masked password field, a Login action, and a Forgot Password flow.
- FR-03: Login validation rejects invalid username and password formats with clear feedback and never exposes the password value.
- FR-04: If both inputs are valid, the interface shows a success message in a status region without navigation or persistence.
- FR-05: Forgot Password hides the login form, reveals the recovery view, and validates the username using the same rules as login.
- FR-06: For recovery, invalid usernames show an error; valid usernames clear the error without creating a reset flow.
- FR-07: Editing the login fields clears stale success feedback.

## Validation rules

- Username: required; 5-20 ASCII letters or digits; no spaces; case-insensitive behavior in both flows.
- Password: required; minimum 8 characters; uppercase letter; lowercase letter; digit; non-whitespace special character; no whitespace; no maximum length.

## Non-functional requirements

- NFR-01: The project remains browser-only; no backend, database, or API is required.
- NFR-02: Inputs must be validated in JavaScript without sending data to a remote service.
- NFR-03: UI remains accessible and keyboard-friendly with visible focus and status messages.

## Out of scope

- User authentication, persistence, password reset delivery, and post-login navigation.
- Any network dependency or server-side account management.
