---
applyTo: ".github/agents/**/*.md,.github/prompts/**/*.md"
---

# Automated Documentation Sync Instructions

Apply these rules to all Automated Documentation Sync capstone agents and prompts.

## 1. Evidence-first behavior

- Start from repository evidence before making any technical or documentation claim.
- Relevant evidence includes Java source, tests, `pom.xml`, configuration files, templates, SQL/data resources, and approved capstone docs.
- Cite the exact file path and, where practical, the class, method, route, config key, or test that supports each material claim.
- If a claim cannot be tied to repository evidence, label it as `Unverified`, `Contradicted by evidence`, or `Unresolved` instead of treating it as fact.

## 2. Source validation and traceability

- Do not fabricate routes, endpoints, classes, methods, dependencies, configuration values, validation logic, or implementation behavior.
- Distinguish confirmed implementation evidence from assumptions, historical intent, and unresolved questions.
- Keep confirmed facts, assumptions, and open questions in separate sections.
- Prefer direct repository-backed statements over narrative summaries.

## 3. Claim classification

For every material technical or documentation claim, classify the result as one of the following:

- `Verified` — supported by repository evidence
- `Unverified` — no direct evidence found
- `Contradicted by evidence` — the repository evidence disagrees with the claim
- `Unresolved` — important but not provable from the current evidence set

Any documentation drift or stale claim must be reported with the supporting evidence and the reason it is contradictory or outdated.

## 4. Drift detection and documentation safety

- Treat stale or contradictory documentation as a detected discrepancy, not as a silent correction.
- The stale README route is a known example of documentation drift and must be reported explicitly with evidence.
- Do not silently edit protected product documentation or application files to make the repo "look consistent" without human approval.
- Keep discrepancy reporting visible and reviewable for human decision-making.

## 5. Protected scope and file safety

The following files and areas are protected and must not be changed without explicit, separate human approval:

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

Do not modify Todo application source code, tests, dependency declarations, runtime configuration, or protected product docs as part of this capstone unless a separate human approval explicitly authorizes a different task.

## 6. Approved capstone scope

- Keep the Automated Documentation Sync workflow strictly documentation-focused.
- Create or modify only approved capstone artifacts under the allowlisted project paths.
- Keep all capstone framework files separate from the existing Todo product documentation and product code.
- Avoid copying unrelated business logic, project-specific rules, or protected implementation decisions from reference projects.

## 7. Human approval gates

- The workflow begins only when a human explicitly invokes it.
- Agent output is never treated as human approval.
- Human approval is required before authorized documentation changes, implementation progress beyond a task gate, verification acceptance, and PR preparation.
- If evidence conflicts, is missing, or is insufficient, stop and ask for a human decision instead of guessing.

## 8. Minimal, maintainable workflow

- Keep the implementation minimal and focused on the documentation-sync workflow.
- Prefer the smallest viable orchestration, agent, and instruction set that satisfies the approved requirements and architecture.
- Do not broaden the scope to unrelated features, product logic, or application behavior.
- Avoid duplicate rule sets where an existing repository instruction already covers the requirement.

## 9. Delivery and output requirements

- Keep analysis separate from proposed documentation updates.
- Ensure generated outputs remain inside approved capstone paths.
- Use clear stage traceability and evidence references for each material claim.
- When a stage is not ready, explain the blocker, required evidence, and missing approval rather than pretending the work is done.

## 10. Final rule

If a fact, route, dependency, rule, route mapping, or configuration value cannot be verified in the repository, it must not be treated as real. Report that uncertainty and stop for human review.
