# Agentic SDLC — Automated Documentation Sync

**Project:** Java 11 Spring Boot Todo application  
**Document:** Requirements  
**Status:** Draft for human review

## 1. Goal

Demonstrate an Agentic SDLC workflow that generates and synchronizes accurate technical documentation from the existing Java 11 Spring Boot Todo repository. This is a documentation use case, not a request to implement another Todo feature.

The supplied assignment information states that no Jira user story was assigned; under that supplied scope, a Jira story is not required.

## 2. Assignment information provided for this capstone

This baseline reflects assignment information supplied by the user in the capstone assignment brief/conversation. The authoritative full rubric was not located in the repository; statements below are not represented as an independent verification of unavailable rubric text.

- The use case is **Agentic SDLC — Automated Documentation Sync**.
- The workflow covers eight stages: Requirements, Architecture, Design Review, Implementation Planning, Implementation, Review, Verification, and Pull Request.
- Technical documentation must describe the existing application accurately and be grounded in repository evidence.
- The Java source, tests, `pom.xml`, application configuration, and relevant repository files must be inspected before documenting technical facts.
- Human approval is required before implementation and before the final PR.
- The supplied assignment information states that no Jira user story was assigned; this is user-provided context, not an independently verified repository fact.

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
- Requiring a Jira story.

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
- The supplied assignment information can be used as the working scope; any additional rubric requirements remain unverified unless the authoritative rubric is made available.

### Unresolved decisions

- Is a separate automatic documentation-sync trigger required by the authoritative rubric? If so, which event should initiate it? No such trigger is assumed in this scope.
- Which existing documentation files, if any, must be updated, rather than producing a separate technical reference?
- Does the authoritative rubric specify further requirements for Copilot agents, prompts, instructions, skills, hooks, verification, or PR artifacts?

## 9. Approval record

**Approval status:** Pending human review.  
Implementation and changes to existing documentation are not authorized until the applicable approval checkpoints are met.
