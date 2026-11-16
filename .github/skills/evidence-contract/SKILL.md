---
name: evidence-contract
description: Standardizes evidence collection, citation, claim classification, and traceable output for the Automated Documentation Sync capstone. Use before writing technical summaries, documentation updates, or verification notes so claims stay grounded in repository evidence.
---

# Evidence Contract

Use this skill whenever a workflow step needs to describe what the repository actually does, compare it to existing documentation, or prepare evidence-backed notes for human review.

## When to use this skill

Use this skill before:

- drafting requirements, architecture, design-review, or implementation-plan content
- summarizing source behavior in a report or artifact
- classifying a claim as Verified, Unverified, Contradicted by evidence, or Unresolved
- preparing implementation review or verification evidence
- writing any technical statement that could be mistaken for fact

This skill is reusable and intentionally narrow: it reduces repeated evidence-gathering logic without duplicating the repo-wide guardrails already enforced by `.github/copilot-instructions.md`.

## Inputs

The caller provides:

- the repository area or file set under review
- the claim or question to validate
- the expected output format or artifact to populate
- any known ambiguity, contradiction, or missing fact

The caller must not assume missing information is true.

## Source authority

The repository is the source of truth for implementation claims.

Use this precedence when evaluating a statement:

1. Java source code, tests, build files, and runtime configuration are the primary evidence for the current application implementation and behavior.
2. Current repository evidence takes precedence over older or descriptive documentation.
3. Approved capstone documents establish approved project decisions, workflow boundaries, and review constraints, but they do not themselves prove undocumented application behavior.
4. Existing documentation is evidence of what the documentation claims, not proof that the implementation behaves that way.
5. When documentation conflicts with implementation evidence, report the discrepancy clearly and do not treat the documentation as authoritative without separate human review.

Documentation can support a claim, but it cannot override the current repository state. When documentation and code disagree, the repository evidence takes precedence and the discrepancy must be reported explicitly.

## Required procedure

### 1. Start from the repository

Inspect the actual source of truth before writing a claim. Relevant repository evidence may include:

- Java source under `src/main/java/**`
- tests under `src/test/java/**`
- `pom.xml`
- resource files under `src/main/resources/**`
- existing product docs under `docs/`
- approved capstone docs under `docs/automated-documentation-sync/`

If a fact is not backed by a repository file, do not treat it as implemented behavior.

### 2. Capture exact evidence

For every material claim, record:

- the exact file path
- the relevant class, method, route, config key, or test
- a short explanation of how the evidence supports the claim
- whether the source is direct implementation evidence, test evidence, documentation, or historical context only

Prefer precise citations over broad statements. For example, cite the route mapping file, the concrete class or controller, or the clear test assertion.

### 3. Separate fact from assumption

Keep these categories distinct:

- Confirmed fact: directly supported by repository evidence
- Assumption: likely or reasonable but not proven by source evidence
- Open question: still unknown or missing from the repository evidence
- Contradiction: repository evidence disagrees with a claim or a doc statement

Do not merge these states together in one paragraph.

### 4. Classify each claim

Use exactly these statuses:

- `Verified` — supported by repository evidence
- `Unverified` — no direct evidence found
- `Contradicted by evidence` — the repo evidence disagrees with the claim
- `Unresolved` — important but not provable from the current evidence set

A claim that cannot be tied to actual code or config is not a verified implementation fact.

### 5. Preserve traceability

When producing output, include:

1. objective
2. repository evidence reviewed
3. confirmed facts
4. assumptions and open questions
5. claim classification table or list
6. human decisions needed

This keeps the output reviewable and directly tied to the repository source.

### 6. Prevent fabrication

Never invent:

- routes, endpoints, classes, or methods
- configuration values or dependencies
- validation rules or runtime behavior
- tests, test results, or commands that were not actually run
- implementation details that do not exist in the repository

If a detail cannot be verified, mark it as `Unverified`, `Contradicted by evidence`, or `Unresolved` and stop for human review.

## Expected output structure

When this skill is used, the output should contain the following sections:

```markdown
## Objective

## Repository Evidence Reviewed

## Confirmed Facts

## Assumptions and Unresolved Questions

## Claim Classification

| Claim | Status | Evidence | Notes |
|---|---|---|---|

## Human Decisions Required
```

This structure supports traceability without requiring a complex workflow.

## Validation and error handling

Validate the result before finalizing:

- Did every material statement cite actual repository evidence?
- Did any claim rely on guesswork, assumptions, or missing facts?
- Did the output clearly distinguish facts from uncertainty?
- Did the output classify claims without collapsing multiple states into one?
- Does the output avoid unsupported implementation details?

If evidence is missing or contradictory, do not guess. Return the uncertainty and request a human decision.

## Boundaries and restrictions

This skill does not authorize file modification. It is proof and traceability logic only.

It must not be used to silently correct protected product documentation or application code. Any edit to protected files requires explicit human approval outside the scope of this skill.

## Summary

This skill enforces the repository-first standard for the capstone: evidence before claims, precise file references, explicit claim status, and honest reporting when evidence is absent or contradictory.
