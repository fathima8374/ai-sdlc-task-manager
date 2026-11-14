# Task Completion Tracking Requirements

**Source story:** [Task Completion Tracking](https://fathima-elitea-docs.atlassian.net/wiki/spaces/Documentat/pages/12746753/Task+Completion+Tracking) (Confluence page 12746753, Documentation space)
**Status:** Implemented scope derived from the human-authorized Confluence story

## 1. User need and business context

As a Todo application user, I want to mark tasks as completed or pending, so that I can track my progress and distinguish unfinished tasks from completed tasks.

The Confluence story states that the current application supports adding, viewing, editing, and deleting tasks, but cannot identify which tasks are completed. The requested feature adds completion tracking without changing the existing task-management behavior.

## 2. Repository facts

- The application is a Java 11 Spring Boot 2.3 application using Spring MVC, Thymeleaf, Spring Data JPA, and H2 (`pom.xml`, `src/main/resources/application.properties`).
- `Task` currently stores an integer ID, description, date, and priority (`src/main/java/com/example/todoapp/domain/Task.java`).
- The browser task list is rendered by `src/main/resources/templates/home.html`; web routes are handled by `TaskController`.
- Persistence uses `TaskService`, `TaskDaoImpl`, and `TaskRepository`. Updates load the stored task and modify selected fields before saving.
- The configured H2 datasource is in-memory. The application already backfills legacy null priorities on startup through `TaskPriorityMigration`.
- Existing automated tests cover task persistence, web behavior and rendering, REST behavior, migration, and context loading.

## 3. Functional requirements

| ID | Requirement | Traceability |
|---|---|---|
| FR-01 | A task has a completion status represented as Pending or Completed. New tasks default to Pending. | Confluence AC 1-3; suggested Boolean note |
| FR-02 | The task list displays each task's current completion status. | Confluence AC 3 |
| FR-03 | A user can change a Pending task to Completed and a Completed task to Pending from the task list. | Confluence AC 1-2 |
| FR-04 | Saving a status change retains the changed status when the task is read again. | Confluence AC 4 |
| FR-05 | Changing completion status does not overwrite the task's ID, description, date, or priority. | Compatibility requirement supporting AC 4 |
| FR-06 | Deleting a task continues to remove it using existing behavior. | Confluence AC 5 |
| FR-07 | Existing create, view, edit, list, and delete behavior remains operational. | Confluence context and AC 5 |

## 4. Non-functional requirements

- Preserve Java 11 and the existing Spring Boot/dependency configuration; no dependency or build-file change is required by this feature.
- Use the existing MVC, Thymeleaf, service, DAO, repository, and JPA patterns.
- Persist status in the task entity and database, not only in the browser or session.
- Use a state-changing POST action; do not mutate completion through a GET request.
- Keep the implementation focused: no sorting, filtering, reminders, audit history, or unrelated task fields are introduced.

## 5. Acceptance criteria and testable scenarios

| AC | Given / When | Then | Verification |
|---|---|---|---|
| AC-1 (story 1) | A Pending task; user selects Mark Completed | Stored status becomes Completed | POST action and repository reload |
| AC-2 (story 2) | A Completed task; user selects Mark Pending | Stored status becomes Pending | POST action and repository reload |
| AC-3 (story 3) | User views the task list | Every task shows Pending or Completed and an action appropriate to its current state | Rendered HTML assertions |
| AC-4 (story 4) | A task's status is changed and the application saves it | Reloaded task retains the new status and all unrelated fields | Persistence/integration assertions |
| AC-5 (story 5) | User deletes a task | The task is removed as before | Existing deletion integration coverage |
| AC-6 (derived default) | A task is newly created or a legacy row has null completion | It is treated as Pending | Entity default and migration test |

## 6. Existing behavior to preserve

- Existing task identity, description, date, priority, create/update/list/view/delete routes and behaviors.
- Existing description and priority validation.
- Existing REST task representation and behavior, except that the newly persisted entity property is naturally available through the existing JSON serialization of `Task`. No REST completion mutation endpoint is required by the story.
- Existing database startup and priority migration behavior.

## 7. Confirmed facts, assumptions, and unresolved questions

### Confirmed facts

- The Confluence page requests two-way completion changes, list visibility, persistence, and unchanged deletion.
- Its suggested implementation is a Boolean `completed` field where `false` means Pending and `true` means Completed; it is guidance, subject to code compatibility.
- The application uses an in-memory H2 datasource and has an existing startup migration runner.

### Assumptions

- Completion changes are initiated from the existing browser task list. The story does not explicitly require a REST endpoint or a separate edit-form control.
- Existing or null database values should be interpreted as Pending, consistent with the story's proposed Boolean mapping and the requested safe default.

### Unresolved questions

- No blocking product questions remain for the scope stated in the story. Behavior for nonexistent task IDs follows the application's existing DAO/controller conventions and is not expanded into a new not-found policy.

## 8. Traceability

| Confluence acceptance criterion | Requirement(s) | Test evidence |
|---|---|---|
| Pending -> Completed | FR-01, FR-03 | Completion integration transition test |
| Completed -> Pending | FR-01, FR-03 | Completion integration transition test |
| Display current status | FR-02 | Thymeleaf rendering assertions |
| Retain changed status after save | FR-04, FR-05 | Repository reload and unrelated-field assertions |
| Deletion continues to work | FR-06, FR-07 | Existing REST delete test and regression suite |
