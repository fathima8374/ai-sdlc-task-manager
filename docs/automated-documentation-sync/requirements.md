# Agentic SDLC — Automated Documentation Sync

**Project:** Java 11 Spring Boot Todo application  
**Document:** Requirements  
**Status:** Prior baseline approved; 2026-11-16 completion amendment awaiting human acceptance

## 1. Goal

Demonstrate an Agentic SDLC workflow that generates and synchronizes accurate technical documentation from the existing Java 11 Spring Boot Todo repository. This is a documentation use case, not a request to implement another Todo feature.

No external story URL or document was supplied with the current request. The human's pasted eight-stage exercise is the actual intake source; external-story support is required when a source is supplied, but an external story must not be fabricated.

## 2. Assignment information provided for this capstone

This baseline reflects the assignment brief supplied in chat. The human supplied the eight-stage exercise again on 2026-11-16 and requested implementation of all eight steps. That supplied text is the exercise source for this amendment, not an independently retrieved Jira/Confluence/Word story.

- The use case is **Agentic SDLC — Automated Documentation Sync**.
- The workflow covers eight stages: Requirements, Architecture, Design Review, Implementation Planning, Implementation, Review, Verification, and Pull Request.
- Technical documentation must describe the existing application accurately and be grounded in repository evidence.
- The Java source, tests, `pom.xml`, application configuration, and relevant repository files must be inspected before documenting technical facts.
- Human approval is required before implementation and before the final PR.
- No external story was provided in the current request. Record the source and actual clarification answers rather than inventing a Jira assignment.

The eight-stage Copilot SDLC workflow is manually invoked by a human, consistent with `.github/copilot-instructions.md`. No automatic repository-change trigger is claimed as implemented. Any separate automatic documentation-sync trigger remains unresolved and is outside this requirements baseline unless separately approved.

## 3. Source of truth and evidence rules

Inspect the relevant repository files before recording technical claims, including:

- Java implementation under `src/main/java/`.
- Automated tests under `src/test/java/`.
- `pom.xml` for Java and dependency declarations.
- `src/main/resources/application.properties` and other relevant runtime configuration.
- Templates, SQL resources, and other application resources under `src/main/resources/`.
- The README and existing documentation under `docs/`, to compare their claims against implementation evidence.

Use source code and configuration to describe current implementation. Use tests to identify behavior asserted by the tests, without claiming that untested behavior is verified. Existing requirements and design documents describe approved intent where their approval is recorded; they do not by themselves prove that implementation matches that intent.

For each material technical claim, provide a file-path citation and, where practical, the relevant class, method, route mapping, configuration key, or test. If evidence is missing, ambiguous, or contradictory, label the uncertainty, describe the conflict, and request a human decision. Do not invent endpoints, behavior, dependencies, configuration, or test results.

Claim-level implementation evidence for the existing application is maintained in `docs/automated-documentation-sync/technical-reference.md`. It cites source, test, configuration, and documentation paths; its test-source inventory is not evidence that tests were executed. New or changed technical claims must still be traceable to their direct repository evidence.

Known README drift must remain visible for human review: the root `README.md:24` describes the Add route as `/api/todo/add/{id}`, while `TaskControllerREST` maps `/api/todo` at `src/main/java/com/example/todoapp/controller/TaskControllerREST.java:18` and `POST /add` at `src/main/java/com/example/todoapp/controller/TaskControllerREST.java:35`, resulting in `POST /api/todo/add` with no path ID. The technical reference documents this discrepancy and the corresponding test-source evidence (`docs/automated-documentation-sync/technical-reference.md:153-155`). The root README is protected and must not be silently corrected.

## 4. Scope and outputs

### In scope

- Produce technical documentation that reflects verified application behavior and identifies supporting repository evidence.
- Detect documentation that is stale or inconsistent with source, tests, dependencies, or configuration; report the discrepancy and propose a correction.
- Keep documented facts, approved intent, assumptions, and unresolved questions distinct.
- Maintain traceability from documented claims to repository evidence and from the SDLC stages to their outputs.

### Out of scope

- Implementing a new Todo feature or changing application runtime behavior as part of this use case.
- Silently overwriting existing approved documents or changing existing decisions.
- Fabricating or requiring credentials for an external story that has not been supplied.

### Stage outputs

The approved implementation plan defines the task sequence and implementation outputs. The selected technical reference is `docs/automated-documentation-sync/technical-reference.md`; its purpose is to summarize the existing application's relevant behavior and evidence, identify documentation drift, and distinguish confirmed facts, assumptions, unresolved questions, and test-source coverage from test execution results.

| Stage | Traceable output |
|---|---|
| 1. Requirements | `docs/automated-documentation-sync/requirements.md` — this requirements baseline. |
| 2. Architecture | `docs/automated-documentation-sync/architecture.md` — evidence model, synchronization approach, boundaries, and traceability. |
| 3. Design Review | `docs/automated-documentation-sync/design-review.md` — design findings and decisions. |
| 4. Implementation Planning | `docs/automated-documentation-sync/impl-plan.md` — approved, dependency-ordered implementation, review, and verification plan. |
| 5. Implementation | The Copilot workflow framework and documentation outputs defined by the approved plan: `.github/agents/orchestrator.agent.md`, `.github/agents/documentation-sync.agent.md`, the eight stage prompts in `.github/prompts/`, `.github/copilot-instructions.md`, the applicable files in `.github/instructions/` and `.github/skills/`, session tracking in `sessions/README.md` and `sessions/workflow-state.json`, and `docs/automated-documentation-sync/technical-reference.md`. `.github/hooks/session-save.js` is optional and included only as authorized by the plan. |
| 6. Review | `docs/automated-documentation-sync/implementation-review.md` — implementation findings and recommendation, separate from design review. |
| 7. Verification | `docs/automated-documentation-sync/verification.md` — procedures, actual outcomes, and known gaps. |
| 8. Pull Request | `docs/automated-documentation-sync/pr.md` or the final PR description — required PR sections and review evidence. |

This mapping identifies expected outputs; it does not itself record human approval of any stage. The existing `docs/requirements.md`, `docs/architecture.md`, `docs/design-review.md`, and `docs/impl-plan.md`, along with the application, tests, prior decisions, and Git history, must be preserved. Any proposed change to an existing document requires explicit human approval before editing it.

## 5. Acceptance criteria

| ID | Acceptance criterion |
|---|---|
| ADS-001 | The documentation describes the existing Todo application; the work does not add another Todo feature. |
| ADS-002 | Relevant Java source, tests, `pom.xml`, application configuration, and resources are inspected before technical facts are documented. |
| ADS-003 | Material technical claims cite repository evidence by file path and, where practical, a code symbol, route mapping, configuration key, or test. |
| ADS-004 | Stale or conflicting documentation is identified against source evidence; discrepancies are reported for review rather than silently overwritten. |
| ADS-005 | Facts, test coverage, approved intent, assumptions, and unresolved questions are clearly distinguished. No technical facts or test results are invented. |
| ADS-006 | The workflow produces traceable outputs for all eight stages: Requirements, Architecture, Design Review, Implementation Planning, Implementation, Review, Verification, and Pull Request. |
| ADS-007 | Human approval is recorded before implementation begins and before the final PR is prepared or submitted. Changes to existing documentation receive explicit approval before editing. |
| ADS-008 | Implementation review findings are recorded separately from design review, and verification records commands or procedures, actual outcomes, and known gaps. |
| ADS-009 | The final PR description includes **Summary**, **Changes Made**, **Test Evidence**, **Known Limitations**, and **Reviewer Checklist**. |
| ADS-010 | The documentation states its review context or repository revision so that readers can assess whether its evidence may have become stale. |
| ADS-011 | Requirements intake records the actual chat, Jira, Confluence, or Word source, a user story, actual clarification responses, and unresolved assumptions. Approved requirements are committed only when authorized. |
| ADS-012 | Architecture includes component and data-flow diagrams and technology choices. |
| ADS-013 | Implementation review explicitly assesses Correctness, Security, Error Handling, Test Coverage, Code Clarity, DRY Principle, and Dependency Safety. |
| ADS-014 | Repeatable workflow tests cover happy paths, missing/empty evidence, malformed state, missing fields, path escapes, stale approval hashes, denied/pending approvals, claim classification, and repeatability. |
| ADS-015 | Isolated hook tests execute valid, invalid, append, metadata-only, failure, and state-preservation scenarios without writing real session logs. |
| ADS-016 | Verification CI runs workflow, document, hook, and existing application tests. It never writes documentation or grants approvals. |
| ADS-017 | Delivery includes a changelog, actual file inventory, real console output or CI links, and the actual existing/new PR URL. No duplicate PR or unauthorized merge is allowed. |
| ADS-018 | Historical approvals remain historical; amended artifacts require current human acceptance before ready-to-merge status. |

## 5A. Current user story, functional and non-functional requirements

> As a Todo application maintainer, I want Copilot to execute a traceable eight-stage documentation-sync workflow, report source/documentation drift, verify the resulting artifacts, and deliver a reviewable PR, so that technical documentation is reliable without changing application behavior.

| ID | Type | Requirement and acceptance measure |
|---|---|---|
| FR-ADS-01 | Functional | Read the supplied source and record source identity and clarification decisions; never claim an unavailable external source was read. |
| FR-ADS-02 | Functional | Produce all eight stage artifacts, a cited technical reference, and a changelog; structural validation must find them. |
| FR-ADS-03 | Functional | Compare claims with source and return one of the four defined classifications; known README route drift remains visible. |
| FR-ADS-04 | Functional | Block readiness on missing artifacts, pending/rejected decisions, or a changed approved-artifact hash. |
| FR-ADS-05 | Functional | Reuse the branch's existing PR, or create one only when authorized; record real delivery state. |
| NFR-ADS-01 | Non-functional | Resolve evidence paths inside the repository and reject traversal/reparse escapes, empty evidence, and malformed state with explicit failures. |
| NFR-ADS-02 | Non-functional | Checks are read-only and repeatable; fixtures must not mutate actual application or approval records. |
| NFR-ADS-03 | Non-functional | No application behavior/dependency changes; audit output is limited to completion metadata and excludes raw prompts/tokens. |
| NFR-ADS-04 | Non-functional | Reuse PowerShell, Maven, and Node built-in testing; record exact runtime versions and nonzero outcomes. |
| NFR-ADS-05 | Non-functional | Preserve historical decisions, but do not use them to approve unseen amendments or bypass human merge review. |

## 5B. Source and clarification record - 2026-11-16

The source is the exercise pasted by the human in this conversation. A previous question offered a repository-specific documentation-sync guide, workflow changes, or task-priority work; the human was unavailable and no answer was fabricated. The human subsequently clarified: "not partially implement all the eight steps completely and please do change accordingly."

This authorizes completing the documentation-sync implementation and updating its delivery artifacts, not inventing stakeholder approval of the resulting revision. Defaults retained from repository context are documentation-only scope, manual generation, existing Todo evidence, and protected application files. No Jira/Confluence URL or Word path is available; importing a particular external story remains conditional on that source being supplied. There is no unresolved technical design choice blocking this implementation. Acceptance of the new artifacts and merge remain human decisions.

## 6. Human approval checkpoints

1. Review and approve this requirements baseline and the documentation scope.
2. Review and approve the architecture, design review, and implementation plan before implementation.
3. Approve any specific changes to existing documentation before those edits are made.
4. Review implementation findings and verification evidence.
5. Approve the final PR description and contents before PR submission.

Passing tests or generated documentation do not constitute human approval.

## 7. Known limitations

- Documentation can become stale as the source repository changes; citations and a review revision help identify its evidence baseline but do not guarantee ongoing synchronization.
- Tests provide evidence only for the behaviors they exercise.
- The Copilot SDLC workflow is manually invoked; this document does not claim that an automatic repository-change trigger or CI-based documentation synchronization is implemented.
- The technical reference is selected at `docs/automated-documentation-sync/technical-reference.md`; runtime test execution is not claimed by its source-coverage inventory.

## 8. Assumptions and unresolved decisions

### Assumptions

- The existing Todo repository is the application to document, and documentation accuracy—not new application functionality—is the goal.
- The eight-stage exercise supplied on 2026-11-16 is the working rubric; requirements outside that text are not inferred.

### Unresolved decisions

- Is a separate automatic documentation-sync trigger required by the authoritative rubric? If so, which event should initiate it? No such trigger is assumed in this scope.
- Which existing documentation files, if any, must be updated, rather than producing a separate technical reference?
- No further technical decision is required to implement the supplied exercise. Any additional external rubric remains out of scope until supplied.

## 9. Approval record

**Approval history:** The earlier baseline's explicit approval is preserved in workflow state.
**Current amendment:** The human requested implementation on 2026-11-16; acceptance of this changed requirements artifact is pending. Implementation authorization is not acceptance of unseen outputs.
