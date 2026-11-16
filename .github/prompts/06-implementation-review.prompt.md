---
description: Step 6 — review the implementation against the approved plan and scope
author: orchestrator
---

Review the implemented capstone changes against the approved implementation plan, the architecture, and the protected-scope rules. The review is mandatory before verification can be accepted.

Follow `.github/copilot-instructions.md` and the orchestrator requirements. Do not approve work on behalf of the human. This review produces a review artifact, not a final approval.

Before beginning the review, confirm that all implementation tasks scheduled before ADS-PLAN-10 are complete and have passed their required human approval gates. Under the narrow exception in the approved implementation plan and orchestrator, ADS-PLAN-10 may produce and submit its review artifact after its listed task dependencies (ADS-PLAN-04 and ADS-PLAN-06) are completed and human-approved, without prior Implementation-stage approval. This exception authorizes only the review artifact; its acceptance does not approve or complete the Implementation stage and does not authorize Verification. ADS-PLAN-10 must occur before verification; do not require tasks scheduled after ADS-PLAN-10 to be complete before beginning this review. If any preceding task or listed dependency is incomplete or its required approval is missing, stop and request human input; do not begin the review.

Required work:

1. Compare actual changes to the approved plan and dependency order.
2. Check scope compliance and protected-file enforcement.
3. Identify missing work, unauthorized changes, deviations, and defects.
4. Record findings by severity, status, and recommended disposition.
5. Confirm that no application code, tests, runtime configuration, or protected docs were modified without explicit approval.
6. Confirm that the stale README route issue was reported as drift rather than silently corrected.
7. Produce a review artifact suitable for human approval.
8. Evaluate every area below explicitly. Record evidence, findings, disposition, and any unperformed assessment for each area; absence of findings is not proof an area was reviewed.

## Mandatory review checklist

| Review area | Required question |
|---|---|
| Correctness | Does each component satisfy the functional/non-functional requirements and acceptance criteria? |
| Security | Are secrets excluded from output, untrusted input validated, and file/tool boundaries respected? |
| Error Handling | Are API failures, missing files, empty repositories, malformed state, and unavailable tools surfaced without success-shaped fallbacks? |
| Test Coverage | Do unit/integration and workflow checks cover happy paths, Not Found, missing fields, denied approvals, and stale evidence? |
| Code Clarity | Are names and control flow understandable, with explicit inputs, outputs, and failures? |
| DRY Principle | Are shared rules/helpers reused instead of duplicating contradictory logic? |
| Dependency Safety | Are declared/resolved versions recorded and known advisories assessed with cited sources and applicability limits? |

Dependency Safety must not infer safety from a successful build or an empty package manifest. Record scanner/advisory source, date, findings, and limitations; do not claim a comprehensive vulnerability scan unless one actually ran. Do not upgrade protected application dependencies without separate authorization.

Output:
- `docs/automated-documentation-sync/implementation-review.md` or the approved equivalent review record

Stop conditions:
- Stop if the implementation plan is missing or unapproved.
- Stop if the implementation is not within approved scope.
- Stop if required evidence is missing from the review artifact.

Report the review verdict, findings, and unresolved blockers requiring human action.
