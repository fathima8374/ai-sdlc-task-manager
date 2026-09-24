# Task Priority Management Implementation Plan

**Project:** Mini Todo Application  
**SDLC stage:** Step 4 — Implementation Planning  
**Requirements:** `docs/requirements.md`  
**Architecture:** `docs/architecture.md`  
**Design review:** `docs/design-review.md`  
**Status:** Approved after human review

## 1. Implementation overview and scope

This plan implements Task Priority Management as an additive change to the existing Java 11 / Spring Boot 2.3.0 application. It preserves the current entity -> repository -> DAO -> service -> controller structure, route paths, CRUD behavior, task fields, description validation, and task-list ordering.

The implementation will:

- Add a type-safe `Priority` enum containing exactly `HIGH`, `MEDIUM`, and `LOW`.
- Persist priority as a string through JPA.
- Require priority for new web and REST tasks without assigning a request-time default.
- Display priority in the task list and expose it in REST JSON.
- Allow priority changes through the existing web update form and REST PUT endpoint.
- Backfill legacy null priority values to `MEDIUM`.
- Update the existing H2 seed row to explicitly contain `MEDIUM`.
- Return a consistent targeted 4xx validation response for missing, unsupported, malformed, or JSON-binding priority errors.
- Preserve server-owned generated IDs and use the REST update path ID as authoritative.
- Add focused unit, persistence, web, REST, migration, and regression tests using existing dependencies only.

The implementation will not add sorting, filtering, custom priority levels, a new persistence framework, a new API version, authentication, or unrelated refactoring.

## 2. Dependency-ordered task breakdown

Tasks are ordered so that each task can be completed only after its prerequisites are satisfied. Step 5 must not begin until this plan is approved.

| Task ID | Task | Depends on | Status |
|---|---|---|---|
| IP-001 | Establish implementation baseline and test fixtures | None | Blocked pending plan approval |
| IP-002 | Add the type-safe priority domain model | IP-001 | Blocked |
| IP-003 | Extend task persistence and update contracts | IP-002 | Blocked |
| IP-004 | Implement H2 schema, seed, and legacy migration | IP-002, IP-003 | Blocked |
| IP-005 | Add server-side validation and web-controller behavior | IP-002, IP-003, IP-004 | Blocked |
| IP-006 | Update Thymeleaf create, list, and update views | IP-002, IP-005 | Blocked |
| IP-007 | Add REST priority handling and error responses | IP-002, IP-003, IP-004 | Blocked |
| IP-008 | Add unit and persistence tests | IP-002, IP-003, IP-004 | Blocked |
| IP-009 | Add web UI and REST integration tests | IP-005, IP-006, IP-007 | Blocked |
| IP-010 | Run regression verification and finalize implementation evidence | IP-008, IP-009 | Blocked |

### IP-001 — Establish implementation baseline and test fixtures

**Purpose:** Confirm the current behavior that must remain unchanged before modifying production code.

**Expected files to change:**

- `src/test/java/com/example/todoapp/TodoAppApplicationTests.java` — extend the existing smoke-test class only if it is the selected location for shared context/regression assertions.
- New focused test files under `src/test/java/com/example/todoapp/` may be introduced in later testing tasks; no production file changes belong to this task.

**Work:**

- Capture current routes, seed behavior, CRUD operations, and existing description checks as regression expectations.
- Establish test data with stable descriptions, dates, IDs, and priorities once the domain type exists.
- Keep the existing `contextLoads()` test.

**Acceptance criteria:**

- The baseline test suite remains runnable with Java 11 and the existing Maven dependencies.
- Existing route and CRUD expectations are recorded in tests or test fixtures before implementation changes.
- No production behavior is changed by this task.

### IP-002 — Add the type-safe priority domain model

**Purpose:** Define the single canonical set of supported priorities.

**Expected files to change:**

- New `src/main/java/com/example/todoapp/domain/Priority.java`
- `src/main/java/com/example/todoapp/domain/Task.java`

**Work:**

- Create `Priority` with only `HIGH`, `MEDIUM`, and `LOW`.
- Add `Task.priority` using `@Enumerated(EnumType.STRING)`.
- Add accessors and constructors without removing or changing existing `id`, `description`, or `date` semantics.
- Keep new `Task` instances without an implicit `MEDIUM` request default; missing priority remains distinguishable for validation.

**Acceptance criteria:**

- The domain accepts exactly the three enum constants.
- JPA mapping stores textual values rather than ordinal values.
- Existing task fields, accessors, date formatting, and generated ID behavior remain available.
- A newly bound task with no priority remains null until valid input or migration supplies a value.

### IP-003 — Extend task persistence and update contracts

**Purpose:** Carry priority through the existing repository, DAO, and service layers.

**Expected files to change:**

- `src/main/java/com/example/todoapp/dao/TaskDao.java`
- `src/main/java/com/example/todoapp/dao/TaskDaoImpl.java`
- `src/main/java/com/example/todoapp/dao/TaskRepository.java` — expected to remain unchanged; inspect and change only if compilation or persistence mapping requires it.
- `src/main/java/com/example/todoapp/service/TaskService.java`
- `src/main/java/com/example/todoapp/service/TaskServiceImpl.java`

**Work:**

- Extend update method signatures to carry `Priority`.
- Ensure create persistence saves the entity's priority.
- Ensure update loads the existing entity, changes description/date/priority as intended, preserves the generated ID, and saves the same entity.
- Do not add priority-specific finder methods, sorting, or filtering.

**Acceptance criteria:**

- Priority flows from service to DAO to repository on create and update.
- Updating priority does not erase ID, description, or date.
- Existing find-all, find-by-ID, add, and delete operations remain available.
- No additional query is introduced for list rendering.

### IP-004 — Implement H2 schema, seed, and legacy migration

**Purpose:** Make the new column and existing data safe during H2 initialization.

**Mandatory verification gate:** At the beginning of this task, verify the actual Spring Boot 2.3.0 embedded-database and `data.sql` initialization order against the project configuration and H2 behavior. Run a focused startup/integration experiment before selecting the migration mechanism. Do not assume that the planned nullable-column, seed, backfill, and optional non-null sequence works; document the observed order and use only a mechanism proven to work with this application.

**Expected files to change:**

- `src/main/java/com/example/todoapp/domain/Task.java` — column mapping is supplied by IP-002.
- `src/main/resources/data.sql` — explicitly insert `priority = 'MEDIUM'` for the existing seed row.
- `src/main/resources/application.properties` — expected to remain unchanged; change only if the selected H2 initialization ordering requires a minimal existing-property adjustment and that adjustment is verified as necessary.
- A narrowly scoped migration/startup component may be added under `src/main/java/com/example/todoapp/` only if H2 initialization cannot provide the approved idempotent backfill without it. No Flyway, Liquibase, or other dependency may be added.

**Work:**

- Ensure the priority column is available in a nullable startup-compatible state while legacy rows are loaded.
- Explicitly seed the current row with `MEDIUM`.
- Backfill only rows whose priority is null to `MEDIUM`.
- Make the backfill idempotent and execute it before normal task reads/writes expose null priority.
- Preserve all legacy IDs, descriptions, and dates.
- Enforce a database-level non-null constraint only if the selected initialization ordering can guarantee safe startup.

**Acceptance criteria:**

- Application context starts with the new entity and seed data.
- The seed row has `MEDIUM`.
- A legacy row with null priority is backfilled to `MEDIUM`.
- Running initialization/backfill more than once does not alter valid `HIGH`, `MEDIUM`, or `LOW` values.
- No new task receives `MEDIUM` merely because a request omitted priority.
- No legacy ID, description, or date is changed.

### IP-005 — Add server-side validation and web-controller behavior

**Purpose:** Enforce mandatory priority in web create/update flows while preserving current behavior.

**Expected files to change:**

- `src/main/java/com/example/todoapp/controller/TaskController.java`
- `src/main/java/com/example/todoapp/domain/Task.java` — only if binding/validation annotations require a minimal additive change.

**Work:**

- Validate that create and update submissions contain a valid priority.
- Keep existing numeric-only and minimum-five-character description checks unchanged.
- Do not use `MEDIUM` as a create or update fallback.
- On create validation failure, render `home` with the existing task list, error message, and submitted values.
- On update validation failure, render `updateForm` with the submitted task and error message.
- Pass priority through the updated service method.
- Preserve existing redirects and route paths.

**Acceptance criteria:**

- Valid High, Medium, and Low submissions persist and redirect through the existing task-list flow.
- Missing or invalid priority never calls persistence.
- Invalid descriptions remain rejected under the existing rules.
- Validation responses preserve submitted description, date, and priority values where possible.
- Existing home, update, delete, and back-home routes continue to work.

### IP-006 — Update Thymeleaf create, list, and update views

**Purpose:** Make priority visible and editable in the existing UI.

**Expected files to change:**

- `src/main/resources/templates/home.html`
- `src/main/resources/templates/updateForm.html`

**Work:**

- Add a required selector with exactly High, Medium, and Low to the create form.
- Add a Priority column to the task table.
- Show the current priority in each task row.
- Add the same options to the update form and select the current value.
- Preserve existing form actions, description/date controls, links, and visual structure.
- Do not add sorting or filtering controls.

**Acceptance criteria:**

- The create form exposes exactly three understandable choices and marks priority required.
- Every task row displays its priority without changing existing list ordering.
- The update form selects the stored priority and allows changing it.
- Validation redisplays retain the submitted selection.
- Existing task actions and fields remain present.

### IP-007 — Add REST priority handling and error responses

**Purpose:** Extend the existing REST API without changing endpoint paths.

**Expected files to change:**

- `src/main/java/com/example/todoapp/controller/TaskControllerREST.java`
- A narrowly scoped REST validation/error handler under `src/main/java/com/example/todoapp/` if needed for a consistent 4xx response.
- `src/main/java/com/example/todoapp/domain/Task.java` — only for additive JSON binding/serialization behavior.

**Work:**

- Accept priority on `POST /api/todo/add` and `PUT /api/todo/update/{id}`.
- Reject missing, blank, unsupported, malformed, and non-canonical values with one targeted 4xx error shape containing the `priority` field and readable message.
- Convert JSON enum-binding failures into the same error shape as controller validation failures.
- Return priority from both GET endpoints through normal task serialization.
- Treat generated IDs as server-owned on create and the path ID as authoritative on update.
- Preserve existing response messages, endpoint paths, delete behavior, and existing fields.

**Acceptance criteria:**

- Valid canonical priorities are persisted and returned.
- Missing/invalid priority is rejected before DAO persistence.
- Invalid JSON enum text does not produce a success response or a framework-specific inconsistent error shape.
- GET responses include priority for individual tasks and lists.
- Conflicting body/path IDs cannot redirect or overwrite task identity.
- Existing REST CRUD endpoints remain available.

### IP-008 — Add unit and persistence tests

**Purpose:** Verify domain, persistence, update integrity, and migration behavior independently of browser flows.

**Expected files to change:**

- New `src/test/java/com/example/todoapp/domain/PriorityTest.java` — if enum-specific tests are useful.
- New `src/test/java/com/example/todoapp/domain/TaskTest.java` — entity/accessor and validation behavior where appropriate.
- New `src/test/java/com/example/todoapp/dao/TaskDaoImplTest.java` or a Spring Data integration test under `src/test/java/com/example/todoapp/dao/`.
- New migration/startup test under `src/test/java/com/example/todoapp/` or `src/test/java/com/example/todoapp/dao/`.
- `src/test/java/com/example/todoapp/TodoAppApplicationTests.java` — retain and extend only where appropriate.

**Work:**

- Test all enum values and string persistence.
- Test create/update persistence and preservation of ID, description, and date.
- Test null-only legacy backfill and idempotence.
- Test seed data initializes with `MEDIUM`.
- Test that missing new-task priority is not silently defaulted.

**Acceptance criteria:**

- Domain and persistence tests pass on Java 11.
- All three values round-trip without ordinal conversion.
- Legacy null values become `MEDIUM`; existing valid values remain unchanged.
- Create/update tests prove no existing task fields are lost.
- The existing context-load test remains green.

### IP-009 — Add web UI and REST integration tests

**Purpose:** Verify end-to-end controller contracts, views, validation, and API compatibility.

**Expected files to change:**

- New `src/test/java/com/example/todoapp/controller/TaskControllerTest.java` or equivalent Spring MVC test.
- New `src/test/java/com/example/todoapp/controller/TaskControllerRESTTest.java` or equivalent MockMvc test.
- New template/integration test under `src/test/java/com/example/todoapp/` if view rendering needs full context.

**Work:**

- Test GET `/todo/home` model/view and priority display data.
- Test web create/update for all priorities, missing priority, invalid description, redirects, and submitted-value preservation.
- Test REST POST/PUT/GET/DELETE flows.
- Test missing, blank, unsupported, malformed, and non-canonical REST priorities.
- Test consistent 4xx error field/message shape.
- Test list ordering remains unchanged and no priority sorting/filtering is introduced.

**Acceptance criteria:**

- Web tests prove required priority and selected-value behavior.
- REST tests prove valid persistence, additive JSON, validation rejection, and error consistency.
- Existing CRUD and route tests remain green.
- No test requires a dependency not already present in `pom.xml`.

### IP-010 — Run regression verification and finalize implementation evidence

**Purpose:** Confirm the complete enhancement satisfies requirements before Step 5 is considered complete.

**Expected files to change:**

- No production files.
- Test files from IP-008 and IP-009 only if a failing verification reveals an implementation defect.

**Work:**

- Run the existing Maven test command and focused tests.
- Verify startup, H2 seed/migration, web UI flows, REST flows, and existing CRUD.
- Inspect the final diff for scope compliance.
- Confirm no sorting/filtering, unrelated refactoring, dependency additions, or route changes were introduced.

**Acceptance criteria:**

- All existing and new tests pass.
- The application starts successfully and `/todo/home` renders.
- Every returned task has a valid priority.
- Invalid priority is never persisted.
- Existing task fields and CRUD behavior remain intact.
- The implementation diff is limited to approved files.

## 3. Exact expected file change map

The following is the planned Step 5 file map. Files marked conditional must not be changed unless the implementation evidence shows the stated need.

| File | Planned purpose |
|---|---|
| `src/main/java/com/example/todoapp/domain/Priority.java` | New enum with three values. |
| `src/main/java/com/example/todoapp/domain/Task.java` | Add enum field, string JPA mapping, accessors, and additive constructor support. |
| `src/main/java/com/example/todoapp/dao/TaskDao.java` | Carry priority through update contract. |
| `src/main/java/com/example/todoapp/dao/TaskDaoImpl.java` | Persist priority and preserve existing fields during update. |
| `src/main/java/com/example/todoapp/service/TaskService.java` | Carry priority through service contract. |
| `src/main/java/com/example/todoapp/service/TaskServiceImpl.java` | Forward priority to DAO. |
| `src/main/java/com/example/todoapp/controller/TaskController.java` | Web binding, validation, submitted-value preservation, and service calls. |
| `src/main/java/com/example/todoapp/controller/TaskControllerREST.java` | REST priority binding, validation, identity rules, and responses. |
| `src/main/java/com/example/todoapp/controller/...` or another existing package | Conditional narrowly scoped validation/binding error handler only if needed for one 4xx contract. |
| `src/main/resources/templates/home.html` | Required create selector and list priority column. |
| `src/main/resources/templates/updateForm.html` | Current priority selector and update binding. |
| `src/main/resources/data.sql` | Explicit `MEDIUM` seed value. |
| `src/main/resources/application.properties` | Conditional; expected unchanged unless proven necessary for H2 initialization ordering. |
| `src/test/java/com/example/todoapp/TodoAppApplicationTests.java` | Preserve/extend context smoke coverage if appropriate. |
| `src/test/java/com/example/todoapp/domain/*Test.java` | Domain and enum tests as needed. |
| `src/test/java/com/example/todoapp/dao/*Test.java` | Persistence, update integrity, and migration tests. |
| `src/test/java/com/example/todoapp/controller/*Test.java` | Web and REST controller tests. |
| `src/test/java/com/example/todoapp/*IntegrationTest.java` | Optional focused startup/template integration tests using existing dependencies. |

`pom.xml` is explicitly excluded. No new dependency, plugin, migration framework, or test framework is planned.

## 4. Dependencies and blocked tasks

### Dependency chain

```text
IP-001
  -> IP-002
      -> IP-003
          -> IP-004
              -> IP-005
                  -> IP-006
              -> IP-007
      -> IP-008
IP-005 + IP-006 + IP-007
  -> IP-009
IP-008 + IP-009
  -> IP-010
```

### Blocked tasks

- All tasks are blocked until the human stakeholder explicitly approves this Step 4 plan.
- IP-003 is blocked until the `Priority` type and `Task` field are defined.
- IP-004 is blocked until persistence contracts support priority and the H2 startup sequence can be tested.
- IP-005 and IP-007 are blocked until service/DAO contracts accept priority.
- IP-006 is blocked until the controller model and binding names are finalized.
- IP-008 is blocked until domain, persistence, and migration behavior exists.
- IP-009 is blocked until both web and REST behavior exists.
- IP-010 is blocked until all focused tests are available.
- Step 5 must not begin automatically after plan creation; it requires explicit human approval.

## 5. Testing plan

### Unit tests

- `Priority` contains only the three approved values.
- Task priority accessors and construction do not alter existing field semantics.
- Missing priority remains distinguishable from legacy migration.
- Existing description validation remains covered where it is currently controller-owned.

### Persistence/integration tests

- Enum values persist as `HIGH`, `MEDIUM`, and `LOW` text.
- Create and update retain ID, description, and date.
- Null-only legacy backfill assigns `MEDIUM`.
- Backfill is idempotent and does not overwrite valid priority.
- `data.sql` inserts the seed task with `MEDIUM`.
- Application context starts with the new schema and seed data.

### Web UI tests

- Home page displays the priority selector and task priority column.
- Valid create persists each priority.
- Missing priority redisplays `home` with an error and submitted values.
- Update form selects the stored value.
- Priority update preserves task identity, description, and date.
- Invalid update redisplays the form with selected submitted values.
- Existing redirects, delete links, back-home navigation, and description validation remain intact.

### REST API tests

- POST accepts each canonical priority and returns it.
- PUT changes priority using the path ID.
- GET task/list responses include priority.
- Missing, blank, unsupported, malformed, and non-canonical values return the same targeted 4xx shape.
- Invalid requests do not invoke persistence.
- Client-supplied IDs cannot override generated identity or the path ID.
- DELETE remains compatible.

### Regression tests

- Existing context-load test passes.
- Existing task seed and CRUD behavior pass.
- Existing fields remain in REST JSON.
- Existing route paths and success messages remain.
- List order is unchanged and no sorting/filtering is added.
- No additional per-task priority query is introduced.

## 6. H2 schema, seed-data, and legacy migration plan

The implementation must follow this order:

1. Add the enum-backed `priority` column in a startup-compatible nullable state.
2. Update `data.sql` to insert the current seed row with `priority = 'MEDIUM'`.
3. Execute an idempotent null-only backfill for rows created before priority existed.
4. Verify all rows exposed to controllers have one of the three valid values.
5. Enforce non-null at the database level only if the selected H2 initialization ordering makes this safe.

The migration path must not be implemented as a runtime default in `Task`, controller binding, service logic, or REST parsing. A new request without priority must fail validation; only a legacy persisted null may become `MEDIUM`.

Because the current database URL is in-memory H2 with `DB_CLOSE_DELAY=-1`, data is not durable across application restarts in the current configuration. Tests must still exercise the migration behavior explicitly so the same design remains safe for any future durable deployment.

## 7. Risks, mitigations, and rollback considerations

| Risk | Mitigation | Rollback consideration |
|---|---|---|
| `data.sql` executes before the priority column/backfill is ready | Explicitly include `MEDIUM` in the seed row and test startup ordering. | Revert the feature commit as one unit if startup fails; do not leave a partially changed seed/schema state. |
| Existing rows remain null or valid values are overwritten | Backfill only null rows and test idempotence. | Restore the prior application version only after preserving the database; inspect affected rows before any corrective migration. |
| Missing priority is silently defaulted | Keep migration separate from request binding and assert invalid requests do not call persistence. | Revert controller/service behavior if any request-time fallback is observed. |
| JSON binding and validation produce inconsistent 4xx responses | Use one targeted handler and test enum-binding failures separately. | Revert only the error-handling change if unrelated API errors are altered. |
| Web validation loses submitted values | Bind and redisplay the submitted task; test all invalid paths. | Revert template/controller changes together if forms lose existing behavior. |
| Update signature changes regress CRUD | Keep existing fields and test all CRUD flows. | Revert the additive DAO/service/controller changes as one coordinated set. |
| REST clients send old PUT payloads without priority | Return a clear 4xx as approved; document the intentional contract change. | Do not add an implicit fallback; any compatibility revision requires a new human-approved requirement. |
| Client-supplied IDs alter identity | Make generated ID server-owned and path ID authoritative. | Revert only after verifying no incorrect records were changed. |
| H2 behavior differs from future durable storage | Keep the backfill deterministic and document the durable migration sequence. | Treat durable deployment migration as a separate approved change. |

Rollback means reverting the implementation changes while preserving the approved documentation. Because the current H2 database is in-memory, restart resets current data; any future durable rollback must use a reviewed reverse migration or restore procedure and must not delete unrelated task data.

## 8. Verification checklist and recommended implementation sequence

### Recommended sequence

1. Obtain explicit human approval for this plan.
2. Implement IP-001 and establish regression fixtures.
3. Implement IP-002 and verify enum/entity compilation.
4. Implement IP-003 and verify service/DAO compilation.
5. Implement IP-004 and run context/startup plus migration tests.
6. Implement IP-005 and IP-007, then run focused controller/API tests.
7. Implement IP-006 and verify Thymeleaf rendering and form value preservation.
8. Implement IP-008 and IP-009.
9. Run IP-010, inspect the complete diff, and verify scope.
10. Stop for the next applicable human approval gate; do not infer approval from passing tests.

### Final verification checklist

- [ ] Only approved implementation files changed; `pom.xml` was not modified.
- [ ] `Priority` contains exactly `HIGH`, `MEDIUM`, and `LOW`.
- [ ] JPA stores priority as string, not ordinal.
- [ ] New web and REST tasks require priority and never default it.
- [ ] Existing null rows receive `MEDIUM` exactly once or idempotently.
- [ ] Seed data explicitly includes `MEDIUM`.
- [ ] Invalid priority returns the required web error or consistent REST 4xx response.
- [ ] REST binding failures identify `priority` and include a readable message.
- [ ] Submitted web description, date, and priority survive validation failures.
- [ ] REST create IDs are server-owned and update path IDs are authoritative.
- [ ] Existing IDs, descriptions, dates, routes, CRUD operations, and response fields remain intact.
- [ ] Priority is displayed without sorting or filtering.
- [ ] No additional per-task query is introduced.
- [ ] Unit, persistence, web, REST, migration, and regression tests pass.
- [ ] Java 11 and Spring Boot 2.3.0 compatibility is preserved.

## 9. Human approval checkpoint

This Step 4 planning document was explicitly approved by the human stakeholder after review. The approved technical scope, task dependencies, and constraints remain unchanged. Before Step 5 — Implementation begins, the human stakeholder has required that IP-004 first verify the actual Spring Boot 2.3.0 and H2 initialization order through testing rather than assumption.

The approved plan includes:

- the dependency order and task boundaries;
- the exact expected file map and conditional migration/error-handler files;
- the H2 nullable-initialization, explicit-seed, idempotent-backfill sequence;
- mandatory priority with no request-time `MEDIUM` fallback;
- the consistent targeted REST 4xx error contract;
- preservation of web submitted values and existing CRUD/API behavior;
- the testing and rollback approach.

Step 5 — Implementation must not begin automatically or implicitly. The IP-004 verification gate must be completed at the beginning of implementation, and its result must guide the migration mechanism.
