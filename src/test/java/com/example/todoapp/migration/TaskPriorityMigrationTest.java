package com.example.todoapp.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskPriorityMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskPriorityMigration migration;

    @Test
    void seedTaskIsInitializedWithMediumPriority() {
        String priority = jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = 1", String.class);

        assertThat(priority).isEqualTo("MEDIUM");
    }

    @Test
    void backfillUpdatesOnlyNullPrioritiesAndIsIdempotent() {
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (98, '2020-02-01', 'Legacy task', NULL)");
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (99, '2020-02-02', 'High task', 'HIGH')");
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (100, '2020-02-03', 'Medium task', 'MEDIUM')");
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (101, '2020-02-04', 'Low task', 'LOW')");

        migration.backfillNullPriorities();
        migration.backfillNullPriorities();

        assertThat(priorityFor(98)).isEqualTo("MEDIUM");
        assertThat(priorityFor(99)).isEqualTo("HIGH");
        assertThat(priorityFor(100)).isEqualTo("MEDIUM");
        assertThat(priorityFor(101)).isEqualTo("LOW");
        assertThat(descriptionFor(98)).isEqualTo("Legacy task");
        assertThat(dateFor(98)).isEqualTo("2020-02-01");
        assertThat(descriptionFor(99)).isEqualTo("High task");
        assertThat(dateFor(99)).isEqualTo("2020-02-02");
        assertThat(descriptionFor(100)).isEqualTo("Medium task");
        assertThat(dateFor(100)).isEqualTo("2020-02-03");
        assertThat(descriptionFor(101)).isEqualTo("Low task");
        assertThat(dateFor(101)).isEqualTo("2020-02-04");
    }

    private String priorityFor(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = ?", String.class, id);
    }

    private String descriptionFor(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT description FROM task WHERE id = ?", String.class, id);
    }

    private String dateFor(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT FORMATDATETIME(date, 'yyyy-MM-dd') FROM task WHERE id = ?",
                String.class, id);
    }
}
