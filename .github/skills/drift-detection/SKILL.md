---
name: drift-detection
description: Detects stale, missing, or contradictory documentation by comparing repository evidence against existing documentation. Use before proposing documentation changes so discrepancies are reported explicitly and approved by a human before a protected file is edited.
---

# Drift Detection

Use this skill whenever a documentation claim must be compared against the current repository implementation and a discrepancy may exist.

## When to use this skill

Use this skill when:

- comparing README or docs against the actual source code
- checking whether a route, dependency, config value, or behavior still matches the current repository
- reviewing whether an existing doc is stale or contradictory
- preparing a discrepancy report for human review before any documentation edit
- flagging a documentation gap, omission, or unsupported claim

This skill is intentionally reusable and narrow. It reduces repeated discrepancy-checking logic without replacing the project-level approval gates or the repo-wide rules in `.github/copilot-instructions.md`.

## Inputs

The caller provides:

- the documentation item or artifact to review
- the repository evidence to compare against
- any prior approval or known scope constraints
- the expected output or report format

The caller should assume a discrepancy is possible until the repository evidence proves the documentation is accurate.

## Documentation evidence rules

Use these rules when comparing documentation to repository evidence:

- Documentation is evidence only when it reflects the current repository state and has not been contradicted by newer or more direct source evidence.
- Historical docs, previous design notes, and narrative summaries are not authoritative if the current implementation differs from them.
- Protected product docs and app code are not to be silently corrected; stale claims must be reported as drift and escalated for human review.
- Classify a claim from the available evidence, independently of whether a correction has been approved. A supported contradiction remains `Contradicted by evidence` while correction is pending; use `Unresolved` only when the evidence itself is insufficient to decide.
- The goal is detection and reporting, not silent rewriting.

## Required procedure

### 1. Identify the claim or documentation item

List the exact documentation statement to investigate, such as:

- a route description
- a configuration value
- a class or module description
- a behavior described by a README or generated doc
- a requirement or assumption that appears to be stale

The statement must be specific enough to inspect and cite.

### 2. Gather the repository evidence

Read the relevant implementation files before deciding whether a doc is stale or accurate. Look for evidence in:

- Java source under `src/main/java/**`
- tests under `src/test/java/**`
- `pom.xml`
- `src/main/resources/**`
- allowed capstone docs under `docs/automated-documentation-sync/**`
- relevant product docs under `docs/`

Treat documentation as the claim being evaluated, not as independent proof of implementation behavior.

Prefer current source code, tests, build files, and runtime configuration when validating implementation claims.

Use other documentation to understand context and identify inconsistencies, not to override implementation evidence.

Do not treat a file as a source of truth simply because it is old or historically prominent. The current repository state matters most.

### 3. Compare and classify the relationship

For each doc claim, classify the relationship as one of the following:

- `Verified` — the evidence supports the documentation
- `Unverified` — no direct evidence found
- `Contradicted by evidence` — the repository evidence disagrees with the documentation
- `Unresolved` — the claim matters, but the current repository evidence is insufficient to decide

### 4. Report the discrepancy explicitly

When a doc is stale, missing, or contradictory, report:

- the exact document statement or section
- the exact repository evidence that contradicts or supports it
- why the discrepancy matters
- whether the issue should be treated as drift, missing data, or unsupported documentation
- whether the correction is in a protected file and therefore requires separate human approval

Do not silently correct protected product documentation without explicit human approval.

### 5. Preserve the known stale README route as an example

The stale or incorrect README route is a known drift example and must be documented as a discrepancy rather than being silently fixed.

When the drift is identified, record:

- the file or doc section that is stale
- the actual repository evidence that contradicts it
- the classification and supporting source paths, even when no correction decision has been made
- that correcting protected documentation requires separate explicit human approval

This preserves evidence and avoids accidental edits to protected or user-facing project docs.

### 6. Separate detection from correction

This skill supports detection and reporting only.

It does not authorize:

- direct file edits
- automatic patching of protected docs
- assumption-based replacements
- silently changing historical records to match the current implementation without approval

Any approved correction must occur under a separate human-approved task and a separate, explicit workflow step.

## Expected output structure

When used, the output should contain these sections:

```markdown
## Document or Claim Reviewed

## Repository Evidence Checked

## Discrepancy Classification

| Statement | Status | Evidence | Impact |
|---|---|---|---|

## Why This Matters

## Recommended Human Decision
```

This keeps detection and reporting separate from cleanup or implementation work.

## Validation and error handling

Before finalizing the discrepancy report, check:

- Did the investigation use actual repository evidence?
- Did the claim remain tied to the specific file or route being evaluated?
- Did the report distinguish contradiction from missing evidence?
- Did the output clearly state that a protected-file change would require human approval?
- Did the stale README route remain a documented discrepancy rather than a silent correction?

If the evidence is insufficient to classify a claim, report the uncertainty and request human input instead of guessing. If the evidence supports a contradiction, report it as `Contradicted by evidence` without waiting for correction approval; request human approval only before changing protected documentation or when a disposition decision is needed.

## Boundaries and restrictions

This skill does not change application code, tests, pom configuration, protected docs, or any user-facing documentation without separate approval.

It is for detection, traceability, and review-ready discrepancy handling only.

## Summary

This skill makes documentation drift visible and reviewable. It enforces a standard of evidence-first discrepancy reporting and ensures the repository does not silently hide stale documentation or contradictory claims.
