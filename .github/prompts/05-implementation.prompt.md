---
description: Step 5 — implement the approved capstone tasks in dependency order, one task at a time
author: orchestrator
---

Implement only the next eligible task in the approved implementation plan for the Automated Documentation Sync capstone. It is a documentation-only task; do not modify application code, tests, dependencies, runtime configuration, or protected product documentation.

Follow `.github/copilot-instructions.md`, the orchestrator rules, and the approved plan. Keep the workflow minimal and aligned with the Java Todo repository and the capstone use case.

Begin only after the Implementation Planning stage approval and approval of the exact plan/scope are recorded in `sessions/workflow-state.json`. Before each task, confirm its dependencies and that the human has authorized its specific target files and scope. After completing a task, stop for human review and explicit approval of that exact task/output. Record the decision, decision-maker, timestamp, artifact paths, and any conditions in workflow state. Do not start the next task while approval is pending, denied, absent, or ambiguous. Approval of an individual task/output does not by itself approve the Implementation stage; that stage requires a separate recorded human decision before Review or Verification.

Required work:

1. Confirm the task has all dependencies satisfied.
2. Confirm the task is the next eligible mandatory task.
3. Make only the minimal change required for that task.
4. Validate the created or modified file(s) for correctness and consistency with the plan and architecture.
5. Keep all capstone framework files separate from the existing Todo product docs.
6. Stop and request human review after completing the task; require the explicit task/output approval to be recorded before proceeding to another task.
7. Do not proceed to later tasks automatically or treat task approval as stage approval.

Output:
- The specific capstone file(s) approved by the plan

Stop conditions:
- Stop if the task is not in dependency order.
- Stop if the task touches a protected file without explicit approval.
- Stop if the task would add unrelated business logic or reference-project behavior.
- Stop if a required file or approval is missing.
- Stop after each task until its exact output has explicit human approval recorded in `sessions/workflow-state.json`.
- Stop before Review or Verification unless separate human approval of the Implementation stage is recorded. The narrow ADS-PLAN-10 exception permits only preparing/submitting the review artifact for human review; it does not waive the later stage gate or authorize Verification.

Report the task ID, objective, target file(s), acceptance criteria, validation result, task/output approval status, and separate Implementation-stage approval status. Never infer either approval from agent completion.
