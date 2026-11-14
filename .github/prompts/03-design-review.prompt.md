---
description: Step 3 — review the architecture against the approved requirements and safety rules
author: orchestrator
---

Review the approved requirements and architecture for the Automated Documentation Sync capstone. Evaluate the design for architecture safety, completeness, evidence quality, human approval gates, scope control, and suitability for the Java Spring Boot Todo repository.

Apply the rules in `.github/copilot-instructions.md` and the orchestrator’s protected-file and evidence policies. Do not implement changes. Do not alter application code, tests, or protected project docs.

This is the Design Review artifact/recommendation, not approval of the Design Review stage. After producing the artifact, stop for an explicit human decision. Record approval or denial for the exact artifact and stage in `sessions/workflow-state.json`; do not begin Implementation Planning while approval is pending, denied, absent, or ambiguous. Artifact acceptance alone does not approve the Design Review stage.

Required work:

1. Review the architecture against the eight stages and the approved requirements.
2. Validate the evidence model and source-of-truth strategy.
3. Evaluate separation between the capstone workflow and the existing Todo product documentation.
4. Check the coordination model, including the orchestrator, stage handoffs, and approval gates.
5. Review file allowlist, documentation-only scope, and protected-file enforcement.
6. Check that documentation drift reporting and the stale README route are handled correctly.
7. Check the minimal framework choice: adapt the reference structure without copying unrelated business logic.
8. Produce findings with IDs, severity, evidence, recommendations, and a final verdict.
9. Record any decisions required from a human reviewer.

Output:
- `docs/automated-documentation-sync/design-review.md`

Stop conditions:
- Stop if the architecture is not approved or is incomplete.
- Stop if required evidence is missing and a human decision is needed.
- Stop if the design would allow protected-file changes without explicit approval.
- After completing the artifact, stop and wait for the human's Design Review-stage decision. Do not infer approval from the recommendation or agent output.

Report the design-review verdict, exact artifact path, approval status, and any decisions still required. Record any human approval decision in `sessions/workflow-state.json` against the exact stage and artifact.
