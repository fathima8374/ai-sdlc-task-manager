---
description: Step 1 — define the Automated Documentation Sync requirements baseline and evidence rules
author: orchestrator
---

Use the current repository as the evidence source and produce the requirements baseline for the Java 11 Spring Boot Todo application documentation-sync capstone. Do not implement features or change runtime behavior.

Follow `.github/copilot-instructions.md` and establish the Stage 1 baseline independently from repository evidence. Preserve the existing Todo product documentation, implementation, and tests. Treat the existing `docs/requirements.md` and related product docs as protected unless a human explicitly approves a different change scope.

This stage must establish the requirements independently and stop for human approval before Architecture begins.

Required work:

0. Read the supplied user story from its actual source. For Jira or Confluence, use the authorized connector and record the URL/key; for Word use the available document reader and record its path. Never invent credentials or create a substitute external story. When no external story is supplied, use the human's chat brief as the source and record that decision explicitly. Ask unresolved clarification questions one at a time and capture the human's actual responses, or label unanswered items as assumptions/blockers. Never fabricate a clarification exchange.
1. Confirm the project goal: produce accurate technical documentation for the existing Todo application using an Agentic SDLC workflow and source-backed evidence.
2. Define the scope, constraints, and known limitations of the documentation-sync use case.
3. Identify the relevant source-of-truth files to inspect: Java source, tests, `pom.xml`, runtime config, SQL/data resources, templates, and relevant docs.
4. State the acceptance criteria for accurate documentation, evidence, traceability, and human approval.
5. Separate confirmed facts, assumptions, and unresolved questions.
6. Document the requirement that documentation drift must be reported and not silently corrected.
7. Record the known stale README route as a discrepancy requiring human review rather than silent correction.
8. Require explicit human approval before implementation or changes to existing documentation.
9. Include a user story, functional and non-functional requirements with stable IDs, measurable acceptance criteria, and a source/clarification decision record.
10. Once the exact artifact is approved and committing is authorized, inspect the diff, stage only the requirements artifact, and commit it. Record the actual commit SHA; a commit is not stage approval. Preserve existing approval history when revising a baseline and request acceptance of the changed revision.

Output:
- `docs/automated-documentation-sync/requirements.md`

Stop conditions:
- Stop and request human approval if the workflow trigger is not explicitly confirmed.
- Stop if evidence is missing or contradictory and no human decision exists.
- Stop if the task would require a protected file change without approval.

Report what you reviewed, the requirements created or updated, assumptions, open questions, and any approvals still required.
