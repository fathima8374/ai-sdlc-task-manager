# Session State and Artifact Tracking

This directory stores the active workflow state for the Automated Documentation Sync capstone. It is not a blank reusable template. It records the current project session, the stage progression, the task progression, the human approval status, and the generated capstone artifacts that are currently present in the repository.

## Purpose

The workflow must be resumable and traceable across sessions. This state file stores:

- the current stage and current task
- the status of all eight SDLC stages in order
- the status of individual implementation tasks
- human approvals that have been explicitly recorded
- agent status that reflects execution progress only
- actual artifact paths that exist in the repository
- planned artifact paths that are expected later
- pending decisions, blockers, and open questions
- protected-file restrictions and allowed capstone paths

## Status model

There are four distinct status concepts that must not be conflated:

1. Stage status: the readiness of a workflow stage.
2. Task status: the readiness of an individual implementation task.
3. Agent status: what an agent has completed or reported.
4. Human approval: explicit authorization from a human actor.

A stage is not considered fully complete when its required human approval is still pending. Similarly, an implementation task is not approved merely because an agent reported completion. Human approval is recorded separately and must not be populated from agent output.

## Human approval capture

Each stage approval and task approval must be recorded independently from the agent execution record. The approval payload includes:

- `status`: `pending`, `approved`, or `rejected`
- `humanDecision`: the decision text or summary
- `approver`: the human approver identity or role, if known
- `approvedAt`: ISO 8601 timestamp, only if actual evidence exists
- `approvedStage` or `approvedTask`: the relevant stage or task
- `approvedArtifact`: the relevant artifact path if applicable
- `notes`: conditions or constraints

If no human approval record exists, the relevant stage or task remains pending even if an agent has already produced or reviewed content.

## Implementation stage semantics

The `implementation` stage remains in progress until all required implementation tasks and review gates are complete. This is a stage-level workflow state, not a one-time completion event. Individual tasks such as `ADS-PLAN-05` may complete while the implementation stage still remains active because other implementation tasks, review items, and verification work remain pending.

## Task-level approval

Implementation tasks are approved individually. Each task gets a separate approval record. For example, `ADS-PLAN-05` is tracked as `completed-awaiting-human-review`, but it remains pending human approval until the human explicitly records approval. Agent output cannot create that approval automatically.

## Artifact tracking

Artifact tracking distinguishes actual and planned files. Actual artifacts are files that are known to exist in the repository. Planned artifacts are expected later but not yet created. When a file is unknown or not yet created, it must be recorded as `planned` instead of being invented as an existing file.

## Workflow resume pattern

To resume safely across sessions:

1. Load `sessions/workflow-state.json`.
2. Read the `currentStage`, `currentTask`, `stageStatus`, `taskStatus`, `humanApproval`, and `taskApprovals` values.
3. Confirm whether the relevant task or stage artifact exists and whether the required human approval is present at both the stage level and the task level.
4. Resume only from the next eligible stage or task.
5. Never advance a stage or task without an explicit human approval record where required.
6. Keep agent-generated status separate from human approval records.

Stage approvals and task approvals are independent. A task may be complete from the agent's perspective while still awaiting human approval. The implementation stage stays `in_progress` until all required implementation tasks, implementation review artifacts, verification artifacts, and any required human approvals are complete.

## Optional session-save hook

The orchestrator's `SubagentStop` hook runs `.github/hooks/session-save.js` when a specialist finishes, subject to the active Copilot harness's hook support and enablement requirements. For the Local harness, hooks require a trusted workspace and the `chat.useHooks` setting; this setting does not enable hooks in every Copilot environment. The hook appends one JSON object per line to `sessions/.hook-log.jsonl`; each entry records a timestamp, event name, agent type, and session ID when available. Missing optional event fields are recorded as `null` or use the event's documented fallback.

This JSONL file is a supplementary event audit trail, not the source of truth. It does not update `workflow-state.json`, task status, artifacts, or human approvals; the orchestrator remains responsible for authoritative workflow-state checkpoints. The workflow must remain usable when hooks are disabled or unavailable: resume from `workflow-state.json` and do not rely on the hook log for continuity or approval evidence.

## Protected scope

This directory is reserved for workflow coordination and generated capstone artifacts only. It must not be used to modify the Todo application code, tests, runtime configuration, Maven dependencies, or protected product documentation.

## Allowed capstone paths

- `docs/automated-documentation-sync/**`
- `.github/copilot-instructions.md`
- `.github/agents/orchestrator.agent.md`
- `.github/agents/documentation-sync.agent.md`
- `.github/agents/*.agent.md`
- `.github/instructions/**`
- `.github/skills/**`
- `.github/hooks/**`
- `.github/prompts/**`
- `.github/scripts/**`
- `.github/tests/**`
- `.github/workflows/**`
- `sessions/**`

## Protected files

- `docs/requirements.md`
- `docs/architecture.md`
- `docs/design-review.md`
- `docs/impl-plan.md`
- `src/main/java/**`
- `src/test/java/**`
- `src/main/resources/**`
- `pom.xml`
- `.github/agents/requirements-generator.agent.md`
- `reference/colleague-capstone/**`

## Current amendments and delivery observations

Historical `stageStatus`, `humanApproval`, `taskApprovals`, and older agent notes are retained as a decision history. When a completion amendment changes previously accepted artifacts, consult `activeRevision` first. Its human acceptance remains pending until the human reviews the exact new contents; a request to implement is not acceptance.

`delivery` records actual GitHub observations independently from approvals. On 2026-11-16, PR #1 was observed open on the documentation-sync branch, correcting older prose that claimed no submission. This does not invent a missing historical submission decision. Updating an existing requested review surface does not grant merge permission.

Run the dependency-free harness with `powershell -NoProfile -File .github\tests\workflow.tests.ps1`, then `powershell -NoProfile -File .github\scripts\workflow.ps1 -Action Validate`. Use `-Action CheckPrReadiness` separately; pending acceptance must block readiness even if structural validation succeeds. Node 22 is required for `node --test .github\tests\session-save.test.js`; these tests exercise a copy of the hook in temporary fixtures, not this directory's real log.
