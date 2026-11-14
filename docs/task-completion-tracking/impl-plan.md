# Task Completion Tracking Implementation Plan

**Status:** Executed on `feature/task-completion-tracking`, based on `origin/master`
**Scope:** Confluence story 12746753 only

## 1. Implementation sequence

| Step | Files | Action | Expected outcome |
|---|---|---|---|
| 1 | `src/main/java/com/example/todoapp/domain/Task.java` | Add `completed`, default false | New tasks are Pending and the entity exposes persisted status |
| 2 | `src/main/java/com/example/todoapp/migration/TaskPriorityMigration.java` | Backfill null completion values to false | Legacy null values become Pending without overwriting set values |
| 3 | `src/main/java/com/example/todoapp/dao/TaskDao.java`, `TaskDaoImpl.java` | Add a completion-only update operation | Status persists without changing other fields |
| 4 | `src/main/java/com/example/todoapp/service/TaskService.java`, `TaskServiceImpl.java` | Wire operation through the existing service | Controller follows actual application layering |
| 5 | `src/main/java/com/example/todoapp/controller/TaskController.java` | Add POST state-change route | User can request either target status |
| 6 | `src/main/resources/templates/home.html` | Show status and state-specific action | List supports both transitions and clear visibility |
| 7 | `src/test/java/com/example/todoapp/controller/TaskCompletionIntegrationTest.java` | Add web, persistence, field-preservation, list, and deletion regression coverage | Story behavior has end-to-end automated evidence |
| 8 | `src/test/java/com/example/todoapp/migration/TaskPriorityMigrationTest.java` | Test null completion backfill and preservation of set values | Legacy default behavior is covered |

## 2. Verification plan

- Run the focused completion integration and migration tests.
- Run the full Maven test suite through the project wrapper.
- Package/compile with the project Maven configuration.
- Start the application from the wrapper and request the task-list endpoint; record startup and HTTP result.
- Confirm generated HTML labels/actions for both completion states, persistence after re-read, field preservation, default Pending, migration, and deletion.

## 3. Risks and constraints

- Use Java 11-compatible syntax and the existing Spring Boot 2.3/H2 dependencies.
- H2 is in-memory; the migration behavior is tested against the application-managed schema and does not claim support for an external database.
- Keep all old documentation-sync capstone files untouched. New feature artifacts remain under `docs/task-completion-tracking/`.
- No dependency or `pom.xml` changes are planned.
