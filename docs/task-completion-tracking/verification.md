# Task Completion Tracking Verification

**Branch:** `feature/task-completion-tracking` (based on `origin/master`)
**Environment:** Windows 11 x64, Java 25.0.1, Maven Wrapper / Apache Maven 3.6.3

The project declares Java 11 in `pom.xml`. The available Java command used for these checks is Java 25.0.1; a Java 11 runtime was not found in the inspected standard JDK installation locations, so Java 11 execution remains unverified.

## 1. Automated checks

| Check | Command / evidence | Result |
|---|---|---|
| Focused completion and migration tests | `.\mvnw.cmd -B '-Dtest=TaskCompletionIntegrationTest,TaskPriorityMigrationTest' test` | Final run: 7 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS |
| Complete regression suite | `.\mvnw.cmd -B -q test` | BUILD SUCCESS; Surefire XML: 47 tests, 0 failures, 0 errors, 0 skipped across 9 test classes |
| Compile/package | `.\mvnw.cmd -B -q -DskipTests package` | Exit code 0 |
| Whitespace / patch check | `git diff --check` | No whitespace errors |

The new completion integration tests exercise web creation defaulting to Pending, both status transitions, reload persistence, preservation of ID/description/date/priority, status and action rendering, ordinary edit preservation, and deletion. The migration test verifies null-to-Pending backfill is idempotent and does not overwrite a completed value.

During test development, initial HTML expectations were adjusted to match the actual Thymeleaf output (status inside `<span>` and a line break between button attributes). The final focused and complete suite results below reflect the corrected assertions.

## 2. Runtime startup and route check

1. `.\mvnw.cmd -B spring-boot:run` attempted the default port 8080 but failed to start the web server because port 8080 was already in use. This was an environment port conflict.
2. The packaged application was then started with `java -jar target\todo-app-0.0.1-SNAPSHOT.jar --server.port=18080`.
3. Startup logs confirmed Spring Boot 2.3.0 started Tomcat on port 18080 and reported the application started.
4. `Invoke-WebRequest -UseBasicParsing http://localhost:18080/todo/home` returned HTTP 200. The response contained the Todo page title, `STATUS` column, Pending status, and Mark Completed action.
5. The verification application process was stopped after the smoke check.

The runtime smoke check confirms startup and page rendering. State transitions and persistence were exercised through the automated MockMvc integration tests against the application-managed H2 database.

## 3. Limitations

- Port 8080 was occupied, so runtime smoke verification used port 18080. Default-port startup could not be demonstrated in this environment.
- Tests, packaging, and runtime checks used Java 25.0.1 rather than the project's declared Java 11. Java 11 execution remains unverified.
- The repository configures an in-memory H2 database. The backfill is verified against that schema; no external or durable production database migration was tested.
- Browser interaction was not manually performed; the rendered page, POST behavior, and persistence were covered by integration tests.
- No claim is made for authentication/authorization behavior; the existing application has no such layer.
- No REST completion-mutation endpoint was added because the Confluence story requires task-list interaction and does not specify REST mutation.
