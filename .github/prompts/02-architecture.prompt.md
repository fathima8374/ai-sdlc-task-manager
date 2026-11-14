---
description: Step 2 — define the capstone architecture, evidence model, workflow boundaries, and protected-files policy
author: orchestrator
---

Using the approved requirements baseline, define the architecture for the Automated Documentation Sync capstone on the Java 11 Spring Boot Todo application.

Follow `.github/copilot-instructions.md` and the orchestrator rules. This is Stage 2 and must use the approved requirements baseline as its input. Do not duplicate unrelated business logic from the reference project. Adapt the reference framework only for structure, discipline, and workflow completeness.

This stage must stop for human approval before Design Review begins.

Required work:

1. Define the architecture purpose, scope, and boundaries.
2. Describe the current repository components that must remain protected.
3. Define the source-of-truth strategy: Java source, tests, `pom.xml`, runtime configuration, templates, SQL/data resources, and relevant docs.
4. Describe the eight-stage SDLC flow and each stage's input, output, and human approval gate.
5. Identify the mandatory and optional framework components: orchestrator, documentation-sync specialist, stage prompts, instructions, session tracking, review/verification artifacts, and PR readiness outputs.
6. Define evidence collection and claim validation rules.
7. Define drift detection and discrepancy handling, including the stale README route.
8. Define the protected file list and documentation-only change controls.
9. Identify manual trigger policy and explicitly keep it human-initiated unless a later human approval changes it.
10. Ensure architecture decisions trace back to the approved requirements.

Output:
- `docs/automated-documentation-sync/architecture.md`

Stop conditions:
- Stop if the requirements baseline is not approved.
- Stop if evidence or protection rules are unclear.
- Stop if a proposed architecture would require modifying protected app code or product docs without explicit approval.

Report the architecture, rationale, traceability, and any unresolved decisions requiring human review.
