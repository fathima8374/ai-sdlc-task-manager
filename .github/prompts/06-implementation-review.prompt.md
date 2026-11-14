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

Output:
- `docs/automated-documentation-sync/implementation-review.md` or the approved equivalent review record

Stop conditions:
- Stop if the implementation plan is missing or unapproved.
- Stop if the implementation is not within approved scope.
- Stop if required evidence is missing from the review artifact.

Report the review verdict, findings, and unresolved blockers requiring human action.
