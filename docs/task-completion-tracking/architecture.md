# Task Completion Tracking Architecture

**Source story:** [Task Completion Tracking](https://fathima-elitea-docs.atlassian.net/wiki/spaces/Documentat/pages/12746753/Task+Completion+Tracking)
**Status:** Implemented design

## 1. Existing application architecture

The browser path is Spring MVC `TaskController` -> `TaskService` / `TaskServiceImpl` -> `TaskDao` / `TaskDaoImpl` -> Spring Data `TaskRepository` -> JPA `Task` entity -> H2. Thymeleaf renders `home.html` and `updateForm.html`. The separate `TaskControllerREST` exposes JSON CRUD routes using the same service and persistence layers.

The service is a real layer, not an assumed one. Existing DAO updates load the persisted `Task`, modify the description/date/priority fields requested by the caller, and save that same entity. This is the compatibility point used to preserve completion across ordinary task edits.

## 2. Affected components and decisions

| Component | Change | Rationale |
|---|---|---|
| `domain/Task.java` | Add nullable-wrapper Boolean `completed`, initialized to `false` | Existing constructors and newly created tasks default to Pending; wrapper permits safe backfill of legacy nulls |
| `migration/TaskPriorityMigration.java` | Backfill `completed IS NULL` to `FALSE` during startup | Reuses the existing idempotent application-runner migration pattern |
| `dao/TaskDao.java`, `TaskDaoImpl.java` | Add a focused completion update operation | Loads and updates only completion, preserving ID, description, date, and priority |
| `service/TaskService.java`, `TaskServiceImpl.java` | Expose completion update to the MVC controller | Preserves the existing controller-service-DAO layering |
| `controller/TaskController.java` | Add a POST route accepting the desired completion value | Explicit state transition, not a GET side effect |
| `templates/home.html` | Show Pending/Completed and an action that sets the opposite state | Satisfies visibility and both transitions without requiring the edit form |

No repository query change is needed: `TaskRepository` already supports `findById`, `save`, and `deleteById`.

## 3. Data model and persistence

`Task.completed` is a Boolean with a Java default of `false`. Its JPA column is nullable so the startup runner can normalize null values from existing rows to `FALSE`. New entity instances default to false, while the migration provides a safe default for legacy database rows. The migration is idempotent and updates only null completion values; it does not overwrite already-set statuses.

`TaskRepository` persistence remains the mechanism: load the current entity, set only `completed`, then save. Normal web/REST description/date/priority updates also load the current entity, so they retain completion.

The configured datasource is `jdbc:h2:mem:dataSource` (`application.properties`), and the project uses embedded H2 with Spring Boot's generated JPA schema. The application does not currently have a durable production database migration framework. This feature therefore extends the existing startup backfill and does not claim migration support for an unspecified external database.

## 4. Request/response flow

1. `GET /todo/home` loads tasks through the existing service and renders each completion label and action.
2. The task row's POST form submits the task ID and desired Boolean value to `/todo/setCompletion/{id}`.
3. `TaskController` calls `TaskService.updateCompletionStatus`.
4. `TaskDaoImpl` loads the existing entity, changes only completion, and saves through `TaskRepository`.
5. The controller redirects to `/todo/home`; the next read renders the persisted state.

The desired state is submitted rather than using a blind toggle in persistence, making the button intent explicit. A missing/invalid request parameter follows Spring MVC binding behavior. Existing application security configuration has no authentication/authorization layer; adding one is outside this story.

## 5. Compatibility and security considerations

- Existing add/edit/list/view/delete behavior and paths remain unchanged.
- New tasks and legacy null completion values resolve to Pending.
- The existing edit and REST update paths must preserve completion because their DAO behavior updates selected fields on a loaded entity.
- Completion mutation uses POST, avoiding state mutation through a link/GET.
- The feature introduces no new dependency, permissions model, or user identity assumptions.
- The user story asks for the browser list interaction; no REST mutation API or edit-form control is added.

## 6. Alternatives considered

- A primitive `boolean` cannot represent legacy null values for an explicit startup backfill. A `Boolean` field allows null normalization while its initializer still makes Java-created tasks default to false.
- A generic toggle would act on the status present at request execution, not the action explicitly chosen. The POST submits the target status and names the button accordingly.
- Updating task status through existing full-edit parameters risks dropping unrelated form data. A dedicated DAO method modifies only the completion property.
