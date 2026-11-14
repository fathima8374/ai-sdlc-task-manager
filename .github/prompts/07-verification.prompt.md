---
description: Step 7 — verify every material technical claim against repository evidence and produce a verification record
author: orchestrator
---

Verify the capstone implementation and its generated documentation claims against repository evidence. This is a mandatory stage before PR preparation and cannot be skipped.

Follow `.github/copilot-instructions.md`, the orchestrator rules, and the approved prior artifacts. Verification must be explicit, evidence-based, and honest about what was actually checked.

Required work:

1. Validate every material technical claim against repository evidence.
2. Classify each claim as Verified, Unverified, Contradicted by evidence, or Unresolved.
3. Cite file paths and relevant code references such as controller mappings, domain classes, config files, dependency declarations, and tests.
4. Report the stale README route as documentation drift and include the contradiction in the verification record.
5. Record actual commands or review steps executed, if any; do not claim results without evidence.
6. Separate confirmed facts from assumptions and unresolved questions.
7. Produce a verification artifact that is suitable for human review.
8. Confirm that review artifacts and approval gates are satisfied before the workflow advances to PR preparation.
9. Include a compact verification-coverage table for relevant routes, dependency declarations, configuration items, and tests. For every item, state whether it was checked; identify the method as source inspection, test execution, or runtime check; record the actual result and supporting evidence; and explicitly mark it `not checked` or `not run` when applicable.
10. Distinguish source-level inspection from executed tests and runtime checks. Do not imply runtime behavior was validated when only source or configuration was inspected. For test execution, record the exact command, tool/runtime versions, exit status, and actual test totals/results when available; identify unavailable tools and unperformed checks.
11. When adding verification coverage to an already approved report, label the addition with its date and ADS-PLAN task, distinguish it from the original verification evidence, and preserve the original commands, results, limitations, and human approval records. Clarify historical workflow-status statements without changing the recorded approvals.

Output:
- `docs/automated-documentation-sync/verification.md`

Stop conditions:
- Stop if implementation review is not complete and approved.
- Document and classify contradictory evidence in the verification report, including known documentation drift such as the stale README API route. Contradictory evidence alone does not prevent creating the evidence-backed verification report, and reporting a contradiction does not approve or accept it.
- Stop workflow advancement or unsupported conclusions only when required evidence is missing, insufficient, or too ambiguous to support a reliable conclusion.
- Stop if verification would claim success without supporting evidence.
- Stop short of a Verified/runtime-validated conclusion when only source inspection or test-source assertions support the claim; classify the limitation accurately.

Report the actual verification results, any negative findings, and any remaining issues that still require human action.
