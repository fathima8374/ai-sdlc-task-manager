# Todo Application Technical Reference

**Purpose:** Evidence-backed overview of the existing Todo application for the Automated Documentation Sync capstone.  
**Inspection date:** 2026-11-13  
**Repository revision:** `5129c3b84f0c2794e394b8e86c316d4c5bc83024` (`HEAD` at inspection)  
**Status:** Initial source-backed reference for human review

## 1. Scope and evidence rules

This reference describes implementation claims supported by the repository files cited below. Java source, tests, Maven metadata, and runtime resources are primary evidence for current implementation. Existing documentation is treated as a claim to compare, not as proof of application behavior. Approved Automated Documentation Sync documents establish workflow decisions and boundaries, not undocumented application behavior.

The Git revision above identifies the checked-out commit, but it does not capture the complete inspection context: `git status --short` showed the capstone directories and files, including `docs/automated-documentation-sync/`, `.github/`, `sessions/`, and `reference/`, as untracked. Those working-tree files are not represented by that commit alone.

### Claim classifications

- **Verified** — directly supported by current source, configuration, or test assertions.
- **Unverified** — no direct implementation evidence was found for the claim.
- **Contradicted by evidence** — repository implementation evidence disagrees with a documentation claim.
- **Unresolved** — available evidence is insufficient to determine the claim.

Test source establishes what a test asserts; it does not prove that the test was run or passed.

| Claim | Status | Evidence / limitation |
|---|---|---|
| The project declares Java 11 and Spring Boot parent `2.3.0.RELEASE`. | Verified | `pom.xml:5-18`. |
| UI and REST controller base mappings are `/todo` and `/api/todo`. | Verified | `src/main/java/com/example/todoapp/controller/TaskController.java:14-16`; `src/main/java/com/example/todoapp/controller/TaskControllerREST.java:17-19`. |
| `Task.priority` is persisted as an enum string. | Verified | `src/main/java/com/example/todoapp/domain/Task.java:24-26`. |
| The root README's add route `/api/todo/add/{id}` matches the REST implementation. | Contradicted by evidence | `README.md:24` conflicts with `@RequestMapping("/api/todo")` and `@PostMapping("/add")` in `src/main/java/com/example/todoapp/controller/TaskControllerREST.java:18,35`. |
| The application's test suite passed. | Unverified | Test sources exist, but no test execution result is recorded in this reference. |
| The misspelled `spring.datasource.continue-on-erorr` property changes runtime behavior. | Unresolved | The key is present in `src/main/resources/application.properties:8`; this inspection did not validate whether it is recognized or effective. |

## 2. Application overview

**Verified:** The application is a Java 11 Spring Boot Todo application. `pom.xml` sets `java.version` to 11 and declares Spring Boot parent version `2.3.0.RELEASE` (`pom.xml:5-18`). `TodoAppApplication` is annotated with `@SpringBootApplication` and launches through `SpringApplication.run` (`src/main/java/com/example/todoapp/TodoAppApplication.java:5-12`).

**Verified:** The application exposes a server-rendered Todo UI and a REST API. Their controller base mappings are `/todo` and `/api/todo`, respectively (`src/main/java/com/example/todoapp/controller/TaskController.java:14-16`; `src/main/java/com/example/todoapp/controller/TaskControllerREST.java:17-19`).

## 3. Architecture and component responsibilities

| Component | Responsibility supported by evidence |
|---|---|
| Application entry point | `TodoAppApplication` starts the Spring Boot application (`src/main/java/com/example/todoapp/TodoAppApplication.java:5-12`). |
| Web controller | `TaskController` handles form-based task creation, listing, updates, deletion, and navigation under `/todo` (`src/main/java/com/example/todoapp/controller/TaskController.java:14-16,21,50,56,67,75,104`). |
| REST controller | `TaskControllerREST` exposes task retrieval, listing, creation, deletion, updating, and priority-related error responses under `/api/todo` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:17-19,24-31,35-89`). |
| Service | `TaskService` defines task operations; `TaskServiceImpl` delegates them to `TaskDao` (`src/main/java/com/example/todoapp/service/TaskService.java`; `src/main/java/com/example/todoapp/service/TaskServiceImpl.java:12-44`). |
| Data access | `TaskDaoImpl` uses `TaskRepository` for persistence and contains the existing-task update behavior (`src/main/java/com/example/todoapp/dao/TaskDaoImpl.java:13-81`). `TaskRepository` extends Spring Data `CrudRepository<Task, Integer>` (`src/main/java/com/example/todoapp/dao/TaskRepository.java:5-7`). |
| Domain | `Task` is a JPA entity; `Priority` is its priority enum (`src/main/java/com/example/todoapp/domain/Task.java:9-26`; `src/main/java/com/example/todoapp/domain/Priority.java:3-7`). |
| Startup migration | `TaskPriorityMigration` implements `ApplicationRunner` and updates rows with null priority to `MEDIUM` (`src/main/java/com/example/todoapp/migration/TaskPriorityMigration.java:9-23`). |
| UI resources | Thymeleaf templates are in `src/main/resources/templates/`; CSS is in `src/main/resources/static/main.css`. The home template renders the task form, priority options, and task list (`src/main/resources/templates/home.html:1-68`). |

## 4. UI routes

The following paths combine the `/todo` class-level mapping with each method mapping. HTTP methods and handler names are taken from `TaskController`.

| Method | Route | Handler / result |
|---|---|---|
| `POST` | `/todo/addTask` | `addTask`; validates submitted values, adds a task, then redirects to `/todo/home` (`src/main/java/com/example/todoapp/controller/TaskController.java:21-48`). |
| `GET` | `/todo/deleteTask/{id}` | `deleteTask`; deletes by ID and redirects home (`src/main/java/com/example/todoapp/controller/TaskController.java:50-54`). |
| `GET` | `/todo/home` | `findAllTasks`; loads tasks, form model data, and priority choices for the `home` view (`src/main/java/com/example/todoapp/controller/TaskController.java:56-64`). |
| `GET` | `/todo/updateTask/{id}` | `updateTask`; loads a task and priority choices for `updateForm` (`src/main/java/com/example/todoapp/controller/TaskController.java:67-72`). |
| `POST` | `/todo/saveUpdate` | `saveUpdate`; validates and updates a task, then redirects home (`src/main/java/com/example/todoapp/controller/TaskController.java:75-101`). |
| `GET` | `/todo/backHome` | `backHome`; redirects to `/todo/home` (`src/main/java/com/example/todoapp/controller/TaskController.java:104-106`). |

The web controller rejects missing or invalid priority and descriptions that are numeric-only or shorter than five trimmed characters (`src/main/java/com/example/todoapp/controller/TaskController.java:21-45,75-100,110-130`). The home template also declares HTML input constraints for description and date (`src/main/resources/templates/home.html:18-34`). These are implementation facts; this reference does not claim that every possible client-side or server-side validation path has been tested.

## 5. REST endpoints

The following paths combine the `/api/todo` class-level mapping with each method mapping.

| Method | Route | Behavior and response evidence |
|---|---|---|
| `GET` | `/api/todo/task/{id}` | Returns the task from `taskService.findTaskById(id)` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:24-28`). |
| `GET` | `/api/todo/tasksList` | Returns `taskService.findAllTasks()` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:30-33`). |
| `POST` | `/api/todo/add` | Requires a non-null priority, resets the submitted ID to zero, saves the task, and returns a response with task/message; annotated with `201 Created` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:35-49`). |
| `DELETE` | `/api/todo/delete/{id}` | Deletes by ID, returns ID/message, and is annotated with `202 Accepted` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:51-59`). |
| `PUT` | `/api/todo/update/{id}` | Uses the path ID, updates task fields and priority, returns task/message, and is annotated with `202 Accepted` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:61-73`). |

Invalid/missing priorities and unreadable request bodies are converted to `400 Bad Request` responses with `field` and `message` values (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:75-94`). REST tests assert these behaviors, including missing priority, null priority, and invalid enum text (`src/test/java/com/example/todoapp/controller/TaskControllerRESTBaselineTest.java:100-136`).

## 6. Domain model and persistence

**Verified:** `Task` has an integer generated ID, description, date, and priority. The date is mapped as `TemporalType.DATE` and formatted for web binding as `yyyy-MM-dd`; priority is stored as an enum string (`src/main/java/com/example/todoapp/domain/Task.java:9-26`). The enum values are exactly `HIGH`, `MEDIUM`, and `LOW` (`src/main/java/com/example/todoapp/domain/Priority.java:3-7`).

**Verified:** Persistence is backed by Spring Data JPA through `TaskRepository`, with service and DAO layers between controllers and repository operations (`src/main/java/com/example/todoapp/dao/TaskRepository.java:5-7`; `src/main/java/com/example/todoapp/dao/TaskDaoImpl.java:13-81`; `src/main/java/com/example/todoapp/service/TaskServiceImpl.java:12-44`).

**Verified:** The DAO's three-argument update method preserves the stored priority, while its four-argument overload updates priority when requested (`src/main/java/com/example/todoapp/dao/TaskDaoImpl.java:56-81`; asserted by `TaskPersistenceTest.threeArgumentUpdatePreservesExistingPriority` in `src/test/java/com/example/todoapp/dao/TaskPersistenceTest.java:78-91`).

## 7. Seed data and migration

**Verified:** `src/main/resources/data.sql:1` inserts a task with ID `1`, date `2020-01-01`, description `Test description`, and priority `MEDIUM`.

**Verified:** On application startup, `TaskPriorityMigration.run` calls `backfillNullPriorities`, which updates only rows whose priority is null to `MEDIUM` (`src/main/java/com/example/todoapp/migration/TaskPriorityMigration.java:18-23`). `TaskPriorityMigrationTest.seedTaskIsInitializedWithMediumPriority` and `backfillUpdatesOnlyNullPrioritiesAndIsIdempotent` assert seed priority, null-only backfilling, preservation of existing enum values, and repeatability (`src/test/java/com/example/todoapp/migration/TaskPriorityMigrationTest.java:19-60`).

## 8. Runtime configuration and dependencies

### Runtime configuration

**Verified:** `src/main/resources/application.properties` configures an in-memory H2 database at `jdbc:h2:mem:dataSource` with H2 driver, username `sa`, blank password, H2 dialect, and H2 console enabled (`src/main/resources/application.properties:2-5,11,13`). It also contains `spring.datasource.continue-on-erorr=true` and `spring.datasource.generate-unique-name=false` (`src/main/resources/application.properties:8-9`). The property spelling `continue-on-erorr` is transcribed as present; its effect is not established by this inspection.

`#server.port=8181` is commented out (`src/main/resources/application.properties:1`), so it is not evidence that the application is configured to run on port 8181. The root README gives example URLs using port 8080 (`README.md:15,18,20`); this reference does not infer a runtime port from those documentation URLs.

### Declared dependencies

`pom.xml` sets Java 11 and Spring Boot parent `2.3.0.RELEASE` (`pom.xml:5-18`) and declares:

- `spring-boot-starter-data-jpa` (`pom.xml:22-25`)
- `spring-boot-starter-thymeleaf` (`pom.xml:26-29`)
- `spring-boot-starter-web` (`pom.xml:30-33`)
- `spring-boot-starter-validation` (`pom.xml:35-38`)
- `spring-boot-devtools`, runtime and optional (`pom.xml:40-45`)
- H2, runtime (`pom.xml:46-50`)
- `spring-boot-starter-test`, test scope, excluding `junit-vintage-engine` (`pom.xml:51-61`)

These are build declarations, not claims that a particular dependency was exercised at runtime in this inspection.

## 9. Test coverage evidence and execution status

The repository contains test-source evidence in `src/test/java/com/example/todoapp/`. The following describes test coverage by test method, not executed results:

| Test file | Coverage evidenced by test source |
|---|---|
| `src/test/java/com/example/todoapp/TodoAppApplicationTests.java` | Spring application context-load test: `contextLoads` (`:6-11`). |
| `src/test/java/com/example/todoapp/domain/PriorityTest.java` | Asserts the supported enum values: `exposesExactlyTheSupportedPriorities` (`:9-14`). |
| `src/test/java/com/example/todoapp/controller/TaskControllerBaselineTest.java` | Web controller route/view behavior, create/update validation, priority handling, and redirects (for example `homeLoadsTaskListAndCreateTask`, `addTaskRejectsInvalidPriorityBinding`, `backHomeRedirectsToHome`). |
| `src/test/java/com/example/todoapp/controller/TaskControllerRESTBaselineTest.java` | REST retrieval, list, add, update, delete, and priority error responses (for example `findTaskByIdReturnsTask`, `addReturnsCreatedResponseAndDelegatesTask`, `malformedPriorityReturnsFieldSpecificBadRequest`). |
| `src/test/java/com/example/todoapp/controller/TaskPriorityIntegrationTest.java` | Integrated web/REST priority persistence, validation, rendering, ordering, CRUD, and migration behavior (for example `webCreatePersistsHighMediumAndLow`, `restInvalidAndMissingPriorityDoNotPersist`, `legacyNullPriorityIsBackfilledWithoutOverwritingValidValues`). |
| `src/test/java/com/example/todoapp/controller/TaskPriorityTemplateRenderingTest.java` | Priority options, selected value, and safe rendering in templates. |
| `src/test/java/com/example/todoapp/dao/TaskPersistenceTest.java` | Priority persistence and updates, CRUD compatibility, and preservation of priority in the three-argument update path. |
| `src/test/java/com/example/todoapp/migration/TaskPriorityMigrationTest.java` | Seed priority and repeatable null-priority backfill while preserving valid values. |

**Execution status:** This reference records no test command or test-run result. It does not claim that tests passed. Test execution and results must be recorded separately from this source-coverage inventory.

## 10. Assumptions and unresolved questions

### Assumptions

- The technical reference concerns the current repository working tree inspected on the date above, not a future build or deployed environment.
- Routes listed here are formed from the Spring controller class and method mappings; deployment context paths or external proxies are not represented by these source files.

### Unresolved

- Whether the configured `spring.datasource.continue-on-erorr` key is recognized or has an effect; runtime behavior has not been validated here.
- The runtime port and deployment context path outside the checked-in application configuration.
- Whether every behavior in source has test coverage, and whether the listed tests pass in the current environment.
- Whether the root README's route discrepancy should be corrected; that decision requires human review.

## 11. Documentation drift

### Root README REST add route

**Status: Contradicted by evidence.**

- **Documentation claim:** Root `README.md:24` says `Add= /api/todo/add/{id}`.
- **Implementation evidence:** `TaskControllerREST` has base mapping `/api/todo` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:18`) and add mapping `@PostMapping("/add")` (`src/main/java/com/example/todoapp/controller/TaskControllerREST.java:35`), which combines to `POST /api/todo/add` with no path ID.
- **Test evidence:** `TaskControllerRESTBaselineTest.addReturnsCreatedResponseAndDelegatesTask` posts to `/api/todo/add` (`src/test/java/com/example/todoapp/controller/TaskControllerRESTBaselineTest.java:60-80`).
- **Disposition:** Keep this discrepancy visible for human review. Do not silently edit the protected root README as part of this reference task.

No other documentation statements are classified as implementation facts solely because they appear in documentation. Any additional mismatch should be reported with the exact document claim and direct implementation evidence.

## 12. Review limitations

- This is a repository inspection, not a runtime verification report.
- Test methods establish assertions present in source only; no test execution result is recorded here.
- The checked-out commit SHA does not include the untracked capstone artifacts visible in `git status --short`; this reference itself is part of that working-tree context until committed.
- Human review is required before accepting this technical reference as an approved artifact or using it to authorize changes to protected documentation.
