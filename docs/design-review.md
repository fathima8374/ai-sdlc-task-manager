# Task Priority Management Design Review

**Project:** Mini Todo Application  
**Review step:** Step 3 — Design Review  
**Reviewed documents:** `docs/requirements.md`, `docs/architecture.md`  
**Repository baseline:** Spring Boot 2.3.0.RELEASE, Java 11, Spring MVC, Thymeleaf, Spring Data JPA, H2, and the existing entity -> repository -> DAO -> service -> controller flow  
**Status:** Approved after human review

## 1. Review summary

The proposed architecture is directionally correct and preserves the existing application structure. A type-safe `Priority` enum persisted with `EnumType.STRING` is compatible with Java 11, Spring Boot 2.3.0, JPA, Jackson, and Thymeleaf. No new framework is required.

The review identified implementation risks around H2 initialization order, the existing `data.sql` seed row, JSON enum-binding failures, consistent 4xx responses, web form value preservation, and generated task IDs. These have been resolved in `docs/architecture.md` as design constraints for Step 5 — Implementation. The approved product requirements remain unchanged.

No Java code, HTML templates, tests, `pom.xml`, or other production files were modified.

## 2. Findings and resolutions

| ID | Severity | Finding | Explanation | Proposed resolution | Decision |
|---|---|---|---|---|---|
| DR-001 | HIGH | Seed SQL omits the new column | The current `data.sql` inserts only `id`, `date`, and `description`. A future required `priority` column could make startup fail, or an omitted value could violate the no-default rule. | Update the seed insert during Step 5 — Implementation to explicitly set `priority` to `MEDIUM`. Do not rely on a database default for new tasks. | **Resolved.** Architecture now requires an explicit `MEDIUM` seed value. |
| DR-002 | HIGH | H2 schema and backfill ordering was underspecified | Spring Boot 2.3 uses embedded-database initialization and the repository has no Flyway/Liquibase migration framework. Adding a non-null column before `data.sql` runs can fail before a backfill executes. | Add the column in a startup-compatible nullable state, load compatible seed data, run an idempotent null-only backfill to `MEDIUM`, then enforce non-null only if initialization ordering safely permits it. | **Resolved.** Architecture specifies the startup sequence and prohibits relying on omitted-column defaults. |
| DR-003 | HIGH | Invalid JSON enum text can bypass controller validation | Jackson can reject malformed or unsupported enum text during request binding before the controller method runs. Without targeted handling, missing/invalid values may return different framework-shaped errors. | Handle JSON binding failures and controller validation failures through one targeted application-level 4xx response that identifies `priority` and includes a human-readable message. | **Resolved.** Architecture now requires one consistent validation-error shape. |
| DR-004 | HIGH | Mandatory priority must be enforced on both create and update | The current controllers perform only description checks and the service accepts tasks without priority. A UI `required` attribute alone is bypassable, and a REST client can omit the field. | Validate non-null, canonical enum values server-side before calling persistence. Missing or invalid values must not reach the DAO. `MEDIUM` is only for legacy migration, never request-time fallback. | **Resolved.** This is explicit in the architecture and follows FR-002, FR-011, and FR-013. |
| DR-005 | MEDIUM | Web validation currently loses submitted values | `TaskController.addTask` reconstructs `createTask` as a new `Task` after description validation fails. Adding priority without changing this pattern could lose the selected priority and other submitted values. | Return the bound submitted task to `home` or otherwise repopulate all valid submitted values, including priority, with an error message. Apply the same rule to `updateForm`. | **Resolved.** Architecture requires preserving the submitted task on validation failure. |
| DR-006 | MEDIUM | REST error contract is not currently centralized | Existing REST controllers have no exception handler or validation response contract. Adding several ad hoc responses could create inconsistent clients and broad exception handling. | Add only targeted handling for priority validation and enum binding, with a stable field/message error shape. Do not redesign unrelated errors or silently catch failures. | **Resolved.** Architecture limits the change to targeted validation handling. |
| DR-007 | MEDIUM | REST entity identity can be client-controlled | `Task` is currently bound directly from JSON and contains a generated `id`. A client-supplied body ID must not override generated identity on create or conflict with the `{id}` path on update. | Treat create IDs as server-owned and use the update path variable as authoritative. Ignore or reject conflicting body IDs without changing the endpoint paths. | **Resolved.** Architecture now records this safety rule. |
| DR-008 | MEDIUM | PUT input compatibility changes intentionally | Existing clients may send description and date without priority. The approved requirements explicitly make missing priority invalid on update, so those clients will receive 4xx after the feature is deployed. | Preserve endpoint paths and existing fields, but document the mandatory priority input as an intentional contract change. Update API clients and tests accordingly. | **Accepted by approved requirements.** No compatibility fallback is permitted. |
| DR-009 | LOW | Enum serialization and case behavior need a single canonical form | Allowing mixed-case values or ordinal persistence could create unstable database/API data. | Persist and serialize canonical uppercase `HIGH`, `MEDIUM`, and `LOW`; render title-cased labels only in the UI; reject non-canonical REST values unless a different policy is explicitly approved. | **Resolved.** Architecture selects string enum persistence and strict canonical REST values. |
| DR-010 | LOW | Current test coverage cannot detect priority regressions | The only existing test is `contextLoads()`. It will not detect missing fields, migration failures, incorrect templates, or inconsistent REST errors. | Add focused persistence, migration, web MVC, REST, validation, and CRUD regression tests using the existing Spring Boot test dependencies. | **Resolved.** Architecture defines the required test layers without adding a test framework. |

## 3. Final design decisions

1. **Priority type:** Add a Java enum with exactly `HIGH`, `MEDIUM`, and `LOW`.
2. **Persistence:** Map the enum with JPA `EnumType.STRING` to a text `priority` column; do not use ordinal values.
3. **New tasks:** Require a non-null valid priority in both web and REST create flows. Never assign a request-time default.
4. **Updates:** Require a non-null valid priority in both web and REST update flows. The path ID is authoritative for REST updates.
5. **Legacy migration:** Backfill only null legacy values to `MEDIUM`, idempotently, before normal reads and writes.
6. **H2 seed:** Change the existing `data.sql` seed row to explicitly include `MEDIUM`.
7. **Schema ordering:** Keep the column nullable while the H2 startup backfill is safely performed; enforce a database-level non-null constraint only when the selected initialization sequence supports it.
8. **REST validation errors:** Return one targeted 4xx error shape for missing/invalid priority and JSON enum-binding failures, identifying the `priority` field and a readable message.
9. **Web validation errors:** Redisplay the existing form with the submitted task values and an error; do not replace the task with a new default object.
10. **Compatibility:** Preserve existing routes, task fields, CRUD operations, list ordering, description validation, and response success messages. Sorting and filtering remain out of scope.
11. **Runtime baseline:** Use only Java 11 and dependencies already present in the Spring Boot 2.3.0 Maven project.
12. **Identity safety:** Generated IDs remain server-owned; a request body must not override the update path identity.

## 4. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Application startup fails because `data.sql` runs against an incompatible schema | Make the seed insert explicitly include `MEDIUM`; test startup with the new entity and initialization sequence. |
| Existing tasks remain null after upgrade | Run an idempotent null-only backfill and test both null legacy rows and already-migrated rows. |
| New requests silently receive `MEDIUM` | Keep migration logic separate from request binding and assert that missing create/update priority is rejected. |
| Jackson enum binding returns a different error shape | Test malformed JSON enum values and route binding exceptions through the same targeted 4xx handler. |
| Submitted UI values disappear after validation failure | Test invalid create and update submissions and assert description, date, and priority remain available to the view. |
| Existing CRUD behavior regresses while update signatures change | Test create, list, find, update, delete, redirects, existing fields, and the REST response structure. |
| An API client relies on the old incomplete PUT payload | Document the intentional mandatory-priority contract and return a clear 4xx message rather than silently defaulting. |
| Client-supplied IDs overwrite generated identity | Ignore or reject create IDs and use the path ID for updates; add a regression test for conflicting IDs. |
| H2 in-memory behavior hides durable-database migration issues | Keep the H2 initialization deterministic and document the equivalent durable migration sequence for future deployment. |

## 5. Human approval checklist

The following items require explicit human approval before Step 4 — Implementation Planning begins:

- [ ] The reviewed architecture and these resolutions comply with `docs/requirements.md`.
- [ ] The Java enum representation and `EnumType.STRING` persistence approach are approved.
- [ ] The H2 startup sequence is approved: explicit `MEDIUM` seed, nullable initialization state, idempotent null-only backfill, then non-null enforcement only when safe.
- [ ] Mandatory priority on both POST and PUT, with no request-time `MEDIUM` fallback, is approved.
- [ ] The single targeted REST 4xx validation-error shape is approved.
- [ ] Web validation must preserve submitted description, date, and priority values.
- [ ] Generated IDs remain server-owned and the update path ID is authoritative.
- [ ] Existing CRUD routes, fields, ordering, description rules, and API behavior remain protected by regression tests.
- [ ] Step 5 — Implementation may modify only the approved production files, templates, tests, and seed/configuration files necessary to implement this design.

**Approval record:** The human stakeholder explicitly approved this design review and the resolved architecture decisions on 2026-09-24. Step 4 — Implementation Planning may proceed; Step 5 — Implementation must not begin outside the approved plan and applicable approval gates.
