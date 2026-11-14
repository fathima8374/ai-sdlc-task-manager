---
description: Step 8 — prepare final PR content only after review and verification are approved
author: orchestrator
---

Prepare the final PR content for the Automated Documentation Sync capstone only after the implementation review and verification artifacts exist and human approval has been recorded.

Follow `.github/copilot-instructions.md`, the orchestrator rules, and the approved implementation plan. Do not create, submit, or merge a PR without explicit human authorization. This prompt can prepare the PR draft, but it cannot bypass the review or verification gates.

## Final readiness gate

Before preparing or declaring PR content ready, inspect the current `sessions/workflow-state.json` and confirm all of the following against the exact artifact paths:

- ADS-PLAN-10, ADS-PLAN-11, and ADS-PLAN-13 are completed and human-approved.
- The Implementation stage is approved.
- `docs/automated-documentation-sync/implementation-review.md` exists and the Implementation Review stage is approved for that artifact, including any conditions.
- `docs/automated-documentation-sync/verification.md` exists and the Verification stage is approved for that artifact.
- Any approval conditions and known limitations are preserved in the PR content.

Artifact existence, task completion, or an approval for a different artifact is insufficient. If a prerequisite or condition is pending, denied, absent, ambiguous, or mismatched, report it as a blocker and stop; do not claim readiness.

Preparing or reviewing the PR description artifact is not creating a GitHub Pull Request. Before creating or submitting an actual GitHub PR, separately confirm that the exact PR draft/task is approved, the Pull Request stage is approved, and explicit human authorization to submit is recorded in workflow state. Do not infer one decision from another. If any of these records is missing or unclear, do not create or submit the PR.

Required work:

1. Perform and report the final readiness-gate checks above before preparing PR content.
2. Produce PR content with the required sections:
   - Summary
   - Changes Made
   - Test Evidence
   - Known Limitations
   - Reviewer Checklist
3. Document known limitations, including any unverified or unresolved claims.
4. Keep the PR limited to approved capstone documentation changes and not the application or protected project docs.
5. State that the stale README route remains a documented discrepancy unless a human approves a specific correction.
6. Require separate human approval of the PR draft, Pull Request stage, and actual submission authorization before creating or submitting an actual GitHub PR.

Output:
- PR description draft or equivalent final PR artifact in the approved capstone location

Stop conditions:
- Stop if review or verification artifacts are missing.
- Stop if any required task or stage approval is not recorded for the exact artifact, or if an applicable condition is unmet.
- Stop if the PR draft/task approval, Pull Request stage approval, or explicit submission authorization is pending, denied, absent, or ambiguous; never create or submit an actual GitHub PR without all required records.
- Stop if the planned PR content would include unrelated project changes, business logic, or protected-file edits.

Report the PR readiness decision and any blockers still awaiting human approval.
