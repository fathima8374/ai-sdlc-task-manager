# Design Review

## Review Summary
The design is intentionally narrow and aligns with the browser-only product requirement. The main risk is accidental overreach into authentication flows, which is guarded by the validation-first architecture.

## Findings
- The app should stay in client-only scope and not introduce an API or database.
- Validation logic must be centralized to keep login and recovery behavior consistent.
- Status messages must never include password content.

## Decisions
- Keep the recovery flow as a username validation step only.
- Preserve accessibility and keyboard usability.
- Use local file loading as the supported entry point.
