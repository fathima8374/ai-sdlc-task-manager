# Agentic SDLC — Automated Documentation Sync Implementation Plan

**Project:** Java 11 Spring Boot Todo application  
**Use case:** GitHub Copilot Capstone — Automated Documentation Sync  
**Status:** Proposed implementation plan for human approval  
**Inputs:** `docs/automated-documentation-sync/requirements.md`, `docs/automated-documentation-sync/architecture.md`, `docs/automated-documentation-sync/design-review.md`  
**Reference:** `reference/colleague-capstone/` (read-only structural reference)

## 1. Purpose

This plan defines the minimal, dependency-ordered implementation needed to build the Agentic SDLC workflow for the documentation-sync use case on the existing Java Spring Boot Todo application.

The plan is intentionally scoped to the documentation workflow only. It does not add application features, change business logic, modify the Todo application code, or edit any protected product documentation without separate approval.

## 2. Planned scope and principles

### 2.1 In scope

- Orchestrator-led eight-stage workflow.
- Documentation-sync specialist agent.
- Eight stage prompts.
- Shared Copilot instructions and minimal reusable skill(s).
- Session state and artifact tracking.
- Evidence collection and claim-level verification.
- Drift detection and discrepancy reporting.
- Human approval gates.
- Implementation review and verification artifacts.
- PR-ready summary template with required sections.

### 2.2 Protected scope

The following are explicitly out of bounds unless a separate human approval is given for a different task:

- `src/main/java/**`
- `src/test/java/**`
- `pom.xml`
- `src/main/resources/**`
- `docs/requirements.md`
- `docs/architecture.md`
- `docs/design-review.md`
- `docs/impl-plan.md`
- `.github/agents/requirements-generator.agent.md`
- any files under `reference/colleague-capstone/`

### 2.3 Core principles

1. Human-initiated trigger only.
2. No fabrication: every technical claim must be backed by source evidence.
3. Documentation drift is reported, not silently corrected.
4. Approval gates are mandatory before stage progression and before PR preparation.
5. The overall reference structure is reused as guidance, not copied wholesale.
6. Keep the implementation minimal and maintainable.

## 3. Implementation outcome summary

The implementation will produce a clean but minimal documentation-sync SDLC framework in the existing repo, aligned to the colleague reference but scaled to the Todo application.

Planned final framework shape:

- `.github/agents/orchestrator.agent.md` — coordination and gate enforcement
- `.github/agents/documentation-sync.agent.md` — evidence-driven documentation generation
- `.github/prompts/01-requirements.prompt.md` through `08-pr.prompt.md` — stage-specific workflow prompts
- `.github/instructions/` — guardrails for evidence, scope, review, and approval
- `.github/skills/` — optional reusable logic where justified
- `sessions/` — session state and artifact tracking
- `generated/` — stage outputs and working documents if needed
- `docs/automated-documentation-sync/` — capstone artifacts and review outputs

This is the minimum viable structure to satisfy the assignment while preserving the existing Todo application and product docs.

## 4. Implementation task catalog

Each task below is intentionally dependency-ordered and is scoped to implementation planning only.

| ID | Stage | Task | Objective | Target file/folder | Dependencies | Priority | Required? | Acceptance criteria |
|---|---|---|---|---|---|---|---|---|
| ADS-PLAN-01 | Foundation | Define workflow contract and protected scope | Establish the boundary between raw application files, protected docs, and capstone outputs | `.github/copilot-instructions.md` or equivalent shared instruction file; plan record | None | P0 | Yes | Scope rules, no-fabrication rule, and protected-file list are explicit and reviewed. |
| ADS-PLAN-02 | Foundation | Create orchestrator agent | Build the human-facing entry point for the eight-stage pipeline | `.github/agents/orchestrator.agent.md` | ADS-PLAN-01 | P0 | Yes | Orchestrator routes stages, maintains state, and blocks progression without approval. |
| ADS-PLAN-03 | Foundation | Create documentation-sync specialist agent | Define the agent responsible for evidence collection and documentation generation | `.github/agents/documentation-sync.agent.md` | ADS-PLAN-01, ADS-PLAN-02 | P0 | Yes | Agent reads repo evidence, flags drift, and does not modify protected app files. |
| ADS-PLAN-04 | Stage prompts | Create stage-run prompts | Add one prompt per SDLC stage | `.github/prompts/` | ADS-PLAN-02, ADS-PLAN-03 | P0 | Yes | 01–08 prompt files exist and correspond to the assignment’s eight stages. |
| ADS-PLAN-05 | Session state | Define session state and output paths | Track stage outputs and approval state through the workflow | `sessions/` and config metadata | ADS-PLAN-02 | P0 | Yes | Session state identifies current stage, approvals, and artifact paths. |
| ADS-PLAN-06 | Guardrails | Add shared instructions | Enforce evidence-first behavior, no-fabrication rules, and scope boundaries | `.github/instructions/` | ADS-PLAN-01 | P0 | Yes | Instructions clearly require citations, source validation, and file allowlist respect. |
| ADS-PLAN-07 | Quality | Add reusable skills | Provide reusable proof for evidence extraction and drift detection | `.github/skills/` | ADS-PLAN-06 | P1 | Optional | Skills exist only if they reduce repetition and are justified by the workflow. |
| ADS-PLAN-08 | Hooks | Add optional session-save hook | Capture workflow audit trail if beneficial | `.github/hooks/session-save.js` | ADS-PLAN-05 | P2 | Optional | Hook is present only if session continuity requires it; otherwise omitted. |
| ADS-PLAN-09 | Documentation outputs | Define technical reference output and evidence contract | Specify the initial documented artifact to generate from source evidence | `docs/automated-documentation-sync/technical-reference.md` (proposed) | ADS-PLAN-03, ADS-PLAN-06 | P0 | Yes | Reference is source-backed, cites files, and explicitly separates confirmed facts from assumptions. |
| ADS-PLAN-10 | Stage outputs | Define implementation review artifact | Create review output that captures issues and disposition | `docs/automated-documentation-sync/implementation-review.md` (proposed) | ADS-PLAN-04, ADS-PLAN-06 | P0 | Yes | Review artifact exists and blocks PR readiness until findings are addressed or accepted. |
| ADS-PLAN-11 | Stage outputs | Define verification artifact | Record actual checks and limitations, not assumed success | `docs/automated-documentation-sync/verification.md` (proposed) | ADS-PLAN-10 | P0 | Yes | Verification artifact clearly separates verified, unverified, and contradicted claims. |
| ADS-PLAN-12 | Stage outputs | Define PR output template | Prepare final PR text with required sections | `docs/automated-documentation-sync/pr.md` or equivalent | ADS-PLAN-10, ADS-PLAN-11 | P0 | Yes | PR output includes Summary, Changes Made, Test Evidence, Known Limitations, and Reviewer Checklist. |
| ADS-PLAN-13 | Workflow | Program the approval gates | Enforce stage review and human sign-off | orchestrator + prompt sequence | ADS-PLAN-02, ADS-PLAN-04 | P0 | Yes | No stage is advanced without a recorded human approval decision. |
| ADS-PLAN-14 | Workflow | Implement drift detection workflow | Detect stale README route and similar mismatches | documentation-sync agent + prompt | ADS-PLAN-03, ADS-PLAN-06 | P0 | Yes | Conflicts are reported with evidence for human review; no silent overwrite. |
| ADS-PLAN-15 | Verification | Define command/test evidence policy | Establish minimal verification standards for generated doc claims | verification artifact + prompt | ADS-PLAN-11 | P0 | Yes | Verification records what was actually checked, including any failed or unavailable checks. |
| ADS-PLAN-16 | Governance | Final review gate and PR readiness check | Confirm all evidence and approvals are satisfied before PR | orchestrator + review flow | ADS-PLAN-10, ADS-PLAN-11, ADS-PLAN-13 | P0 | Yes | PR is blocked until implementation review and verification artifacts are complete and approved. |

## 5. Dependency-ordered execution sequence

The project should be implemented in this dependency order:

1. `ADS-PLAN-01` — Shared guardrails and scope controls
2. `ADS-PLAN-02` — Orchestrator agent
3. `ADS-PLAN-03` — Documentation-sync agent
4. `ADS-PLAN-04` — Eight stage prompts
5. `ADS-PLAN-05` — Session state and artifact tracking
6. `ADS-PLAN-06` — Shared instructions
7. `ADS-PLAN-09` — Technical reference contract
8. `ADS-PLAN-10` — Implementation review output
9. `ADS-PLAN-11` — Verification output
10. `ADS-PLAN-12` — PR template
11. `ADS-PLAN-13` — Approval gates
12. `ADS-PLAN-14` — Drift detection workflow
13. `ADS-PLAN-15` — Verification policy
14. `ADS-PLAN-16` — Final PR readiness gate
15. Optional: `ADS-PLAN-07`, `ADS-PLAN-08` only when justified by workflow complexity

## 6. Stage-by-stage implementation mapping

| SDLC stage | Planned implementation step | Output | Human approval gate |
|---|---|---|---|
| 1. Requirements | Confirm requirements baseline and scope | `docs/automated-documentation-sync/requirements.md` | Required before architecture is approved |
| 2. Architecture | Define evidence model, protected files, workflows, and output boundaries | `docs/automated-documentation-sync/architecture.md` | Required before design review |
| 3. Design Review | Review architecture against requirements and safety boundaries | `docs/automated-documentation-sync/design-review.md` | Required before implementation planning |
| 4. Implementation Planning | Create dependency-ordered task plan | `docs/automated-documentation-sync/impl-plan.md` | Required before any implementation begins |
| 5. Implementation | Create orchestrator, agents, prompts, instructions, session, and documentation outputs | Capstone framework files under `.github/`, `sessions/`, and `docs/automated-documentation-sync/` | Mandatory before continuing |
| 6. Review | Evaluate implementation against requirements and scope | `implementation-review.md` | Mandatory before verification |
| 7. Verification | Run claim-level checks and record actual results | `verification.md` | Mandatory before PR prep |
| 8. Pull Request | Produce PR summary + checklist + review evidence | PR artifact / final description | Mandatory before PR submission |

## 7. Human approval gates and decision records

A human approval gate must be recorded at each major transition.

| Gate | Trigger | Required action |
|---|---|---|
| Gate 1 | Before workflow starts | Human confirms the documentation-sync objective, repository, and allowed files. |
| Gate 2 | After requirements | Human confirms scope and acceptance criteria. |
| Gate 3 | After architecture | Human confirms evidence model and protected-file rules. |
| Gate 4 | After design review | Human confirms the design can proceed to implementation planning. |
| Gate 5 | Before implementation | Human approves exact files to be created or changed and the implementation plan. |
| Gate 6 | Before verification acceptance | Human confirms review findings and verification evidence. |
| Gate 7 | Before PR submission | Human approves final PR content and known limitations. |

No stage may be considered complete because an agent reported it as done; a human approval decision is required.

## 8. Evidence collection and claim validation strategy

The implementation should follow a strict evidence-first claim model.

### 8.1 Source categories

- Controller mappings and route behavior — `src/main/java/com/example/todoapp/controller/**`
- Domain model — `src/main/java/com/example/todoapp/domain/**`
- Runtime config — `src/main/resources/application.properties`, resource files, SQL files
- Dependencies — `pom.xml`
- UI behavior — `src/main/resources/templates/**`, static assets
- Tests — `src/test/java/**`
- Historical intent — approved docs under `docs/` but not as proof of current implementation

### 8.2 Claim validation rules

For every technical claim, the agent must:

1. Identify the exact file(s) that support it.
2. Cite the route, class, method, config key, or test name.
3. Record whether the claim is Verified, Unverified, Contradicted, or Unresolved.
4. Explain difference between implemented behavior and historical intent.
5. Record the stale README route mismatch as drift rather than silently editing the README.

### 8.3 Drift detection rules

The documentation-sync workflow must:

- Compare any documentation claim against the latest repository evidence.
- Flag stale or contradictory documentation as a discrepancy.
- Provide an evidence statement and a recommended disposition.
- Require human approval for any edit to a protected document.

## 9. Detailed task notes and acceptance criteria

### ADS-PLAN-01: Define workflow contract and protected scope

- Objective: Establish the minimum workflow contract that prevents scope creep.
- Target: shared instruction file(s) and/or a project README section that explains the capstone rules.
- Dependencies: None.
- Priority: P0.
- Acceptance: Protected files, no-fabrication, and approval gate rules are explicitly documented.

### ADS-PLAN-02: Orchestrator agent

- Objective: Provide the single entry point that coordinates all stages.
- Target: `.github/agents/orchestrator.agent.md`
- Dependencies: ADS-PLAN-01
- Priority: P0.
- Acceptance: Orchestrator delegates to specialized agents, stores stage state, and blocks progression without approval.

### ADS-PLAN-03: Documentation-sync specialist agent

- Objective: Produce source-backed technical documentation without modifying app code.
- Target: `.github/agents/documentation-sync.agent.md`
- Dependencies: ADS-PLAN-01, ADS-PLAN-02
- Priority: P0.
- Acceptance: Agent inspects repository evidence, cites files, flags drift, and never edits protected app files.

### ADS-PLAN-04: Stage prompts

- Objective: Provide a structured prompt for each stage.
- Target: `.github/prompts/01-requirements.prompt.md` through `08-pr.prompt.md`
- Dependencies: ADS-PLAN-02, ADS-PLAN-03
- Priority: P0.
- Acceptance: Prompts are stage-specific, complete, and aligned to the assignment’s eight stages.

### ADS-PLAN-05: Session state

- Objective: Track stage status, approvals, and outputs.
- Target: `sessions/` and supporting metadata files
- Dependencies: ADS-PLAN-02
- Priority: P0.
- Acceptance: Each stage records its output path and approval status.

### ADS-PLAN-06: Shared instructions

- Objective: Formalize reusable rules shared by all agents.
- Target: `.github/instructions/`
- Dependencies: ADS-PLAN-01
- Priority: P0.
- Acceptance: Evidence-first rules, protected scope, approval gates, and documentation drift handling are codified.

### ADS-PLAN-07: Reusable skills

- Objective: Optional reuse of skills to reduce duplication.
- Target: `.github/skills/`
- Dependencies: ADS-PLAN-06
- Priority: P1
- Acceptance: Skills are present only when they materially improve consistency and reduce repeated logic.

### ADS-PLAN-08: Optional hook

- Objective: Lightweight session audit trail only when it adds value.
- Target: `.github/hooks/session-save.js`
- Dependencies: ADS-PLAN-05
- Priority: P2
- Acceptance: Hook is present only if the workflow explicitly needs an audit trail beyond the orchestrator state.

### ADS-PLAN-09: technical-reference output

- Objective: Define the initial documentation artifact that summarizes the application with cited evidence.
- Target: `docs/automated-documentation-sync/technical-reference.md` (proposed)
- Dependencies: ADS-PLAN-03, ADS-PLAN-06
- Priority: P0.
- Acceptance: File includes evidence-backed claims, assumptions, unresolved questions, and drift findings.

### ADS-PLAN-10: Implementation review artifact

- Objective: Create a formal review step after implementation.
- Target: `docs/automated-documentation-sync/implementation-review.md` (proposed)
- Dependencies: ADS-PLAN-04, ADS-PLAN-06
- Priority: P0.
- Acceptance: Review results are separated from design review and are mandatory before verification.

**Narrow approval-gate exception:** ADS-PLAN-10 may execute after its listed task dependencies, ADS-PLAN-04 and ADS-PLAN-06, are completed and human-approved, without prior Implementation-stage approval. This exception authorizes only producing and submitting the implementation review artifact for human review. Acceptance of that artifact does not approve or complete the Implementation stage and does not authorize Verification. All subsequent human approval gates remain in force.

### ADS-PLAN-11: Verification artifact

- Objective: Capture actual commands/results and limitations.
- Target: `docs/automated-documentation-sync/verification.md` (proposed)
- Dependencies: ADS-PLAN-10
- Priority: P0.
- Acceptance: Verification clearly states verified vs unverified claims and any failed or unavailable checks.

### ADS-PLAN-12: PR artifact

- Objective: Produce a PR description that follows the assignment’s required sections.
- Target: `docs/automated-documentation-sync/pr.md` or equivalent final PR stub
- Dependencies: ADS-PLAN-10, ADS-PLAN-11
- Priority: P0.
- Acceptance: Summary, Changes Made, Test Evidence, Known Limitations, and Reviewer Checklist sections are included.

### ADS-PLAN-13: Approval gates

- Objective: Prevent progression without human consent.
- Target: orchestrator and stage prompts
- Dependencies: ADS-PLAN-02, ADS-PLAN-04
- Priority: P0.
- Acceptance: Each stage stops until the human makes a decision.

### ADS-PLAN-14: Drift detection workflow

- Objective: Treat stale documentation as a first-class output of the workflow.
- Target: documentation-sync agent and prompts
- Dependencies: ADS-PLAN-03, ADS-PLAN-06
- Priority: P0.
- Acceptance: README route discrepancy is raised as documentation drift and is preserved for human review.

### ADS-PLAN-15: Verification policy

- Objective: Keep the framework honest about evidence and real execution.
- Target: verification artifact and prompt flow
- Dependencies: ADS-PLAN-11
- Priority: P0.
- Acceptance: Verification clearly indicates if a route, dependency, configuration, or test was actually checked.

### ADS-PLAN-16: Final readiness gate

- Objective: Ensure PR preparation only happens when review and verification are complete.
- Target: orchestrator and PR workflow
- Dependencies: ADS-PLAN-10, ADS-PLAN-11, ADS-PLAN-13
- Priority: P0.
- Acceptance: PR creation is blocked until all required artifacts and approvals are present.

## 10. Traceability to requirements and architecture decisions

| Decision / requirement | Implementation tasks |
|---|---|
| Human-initiate trigger | ADS-PLAN-01, ADS-PLAN-02, ADS-PLAN-13 |
| Complete orchestrator | ADS-PLAN-02, ADS-PLAN-04, ADS-PLAN-05, ADS-PLAN-13 |
| Reference framework adaptation | ADS-PLAN-02, ADS-PLAN-04, ADS-PLAN-07, ADS-PLAN-08 |
| Documentation-only scope | ADS-PLAN-01, ADS-PLAN-03, ADS-PLAN-06, ADS-PLAN-14 |
| Evidence-based documentation | ADS-PLAN-03, ADS-PLAN-06, ADS-PLAN-09, ADS-PLAN-14 |
| File protect list | ADS-PLAN-01, ADS-PLAN-06 |
| Review and verification before PR | ADS-PLAN-10, ADS-PLAN-11, ADS-PLAN-12, ADS-PLAN-16 |
| README drift handling | ADS-PLAN-14, ADS-PLAN-15 |
| Eight-stage SDLC mapping | ADS-PLAN-04, ADS-PLAN-12, ADS-PLAN-13 |

## 11. Risks, assumptions, and unresolved questions

### 11.1 Risks

- Overbuilding the framework by copying too much from the reference project rather than adapting it.
- Allowing undocumented or unreviewed writes to protected files.
- Inventing technical facts when evidence is incomplete.
- Treating a generated report as proof without review and verification.
- Creating automation that is too ambitious before the human confirms scope and trigger.

### 11.2 Assumptions

- The project will keep the human-initiated trigger.
- The scope remains documentation-only.
- The Todo application source and tests remain unchanged unless separately approved.
- Existing product docs remain protected unless a separate approval changes that rule.

### 11.3 Unresolved questions

- Whether a later milestone will require automatic trigger events beyond manual invocation.
- Whether any additional capstone documentation files will be approved for modification.
- Whether the final PR is prepared in this repo or in a delegated repo context.
- Whether the project should include optional skills or hooks beyond the minimal required set.

## 12. Minimum viable implementation recommendation

The smallest safe implementation to satisfy the assignment is:

- One orchestrator agent
- One documentation-sync agent
- Eight prompt files
- Shared instruction files
- A simple session state mechanism
- Mandatory review and verification artifacts
- Final PR template

Everything else should be considered optional and only added if it adds measurable clarity or traceability.

## 13. Implementation readiness

This plan is ready for human approval as a dependency-ordered, doc-only architecture for the capstone framework. It preserves the approved requirements and architecture decisions, keeps the workflow minimal, and enforces the required review and verification gates before PR readiness.

**Decision:** Ready for human approval before Stage 5 — Implementation.
