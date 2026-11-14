# GitHub Copilot Instructions — Automated Documentation Sync Capstone

This repository contains a Java 11 Spring Boot Todo application plus a separate GitHub Copilot capstone implementation for the use case: Agentic SDLC — Automated Documentation Sync.

The capstone workflow must remain documentation-focused and must not change the existing application behavior, tests, or protected project documentation unless a separate human approval explicitly authorizes a different task.

## 1. Human-in-the-loop requirements

- The workflow begins only when a human explicitly invokes it.
- Human approval is required before implementation begins, before any documentation change is accepted, before verification is accepted, and before PR preparation.
- Agent output is never treated as approval.
- If evidence is missing, conflicting, or ambiguous, the agent must report it and request a human decision instead of guessing.

## 2. Guardrails for all capstone agents and prompts

- Never fabricate technical facts, endpoints, dependencies, validation rules, configuration values, or test results.
- Every material claim must be grounded in repository evidence such as Java source, tests, `pom.xml`, `application.properties`, templates, migration files, and existing documentation.
- Keep technical facts, assumptions, and unresolved questions clearly separated.
- Distinguish confirmed implementation behavior from historical or approved intent.
- Preserve the existing Todo application code, tests, and Git history.
- Preserve the existing product documents under `docs/` unless a human approves a separate edit.

## 3. Protected scope and file safety

The following files are protected by design and must not be changed without explicit, separate human approval:

- `docs/requirements.md`
- `docs/architecture.md`
- `docs/design-review.md`
- `docs/impl-plan.md`
- `src/main/java/**`
- `src/test/java/**`
- `pom.xml`
- `src/main/resources/**`
- `.github/agents/requirements-generator.agent.md`
- `reference/colleague-capstone/**`

The capstone may create or modify only approved capstone artifacts, such as the documentation-sync requirements/architecture/design review/implementation-plan documents and any future workflow files that are explicitly allowlisted.

## 4. Evidence-first documentation rules

- Start with source code, tests, configuration, and dependency declarations before writing technical claims.
- Cite the relevant repository path and, where practical, the class, method, route mapping, config key, or test.
- If a claim is unsupported or contradicted, label it as `Unverified`, `Contradicted by evidence`, or `Unresolved`.
- The stale or incorrect README route is a known documentation discrepancy and must be reported as drift evidence, not silently corrected.
- Note if an implementation detail has no evidence in the repository.

## 5. Workflow and stage discipline

This repository follows the eight-stage Agentic SDLC pattern:

1. Requirements
2. Architecture
3. Design Review
4. Implementation Planning
5. Implementation
6. Review
7. Verification
8. Pull Request

The workflow must keep stage outputs traceable to repository evidence and to the human approvals that authorize each stage.

## 6. Mandatory review and verification gates

- Implementation review records are mandatory before verification can be accepted.
- Verification records are mandatory before PR preparation.
- PR preparation is blocked until review and verification artifacts are present and approved.
- The final PR description must include the required sections: Summary, Changes Made, Test Evidence, Known Limitations, and Reviewer Checklist.

## 7. SDLC framing for this repository

The capstone is not a new feature for the Todo app. It is a documentation-sync workflow that explains the repository accurately without altering runtime behavior.

The first implementation task is to establish repo-wide workflow rules and protected scope. Later tasks build the orchestrator, specialized agents, prompts, instructions, state tracking, and verification artifacts required for the capstone workflow.

## 8. Minimal implementation principle

Prefer the smallest viable framework that satisfies the assignment:

- one orchestrator
- one documentation-sync specialist
- stage prompts
- shared instructions
- session state and artifact tracking as needed
- mandatory review and verification artifacts
- PR description template

Avoid copying unrelated business logic or application behavior from reference projects. Adapt the reference framework only for structure, discipline, and workflow completeness.
