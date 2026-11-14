package com.example.todoapp.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskPriorityMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public TaskPriorityMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        backfillNullPriorities();
        backfillNullCompletionStatuses();
    }

    void backfillNullPriorities() {
        jdbcTemplate.update(
                "UPDATE task SET priority = 'MEDIUM' WHERE priority IS NULL");
    }

    void backfillNullCompletionStatuses() {
        jdbcTemplate.update(
                "UPDATE task SET completed = FALSE WHERE completed IS NULL");
    }
}
