package com.example.todoapp.dao;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TaskPersistenceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTasks() {
        taskRepository.deleteAll();
    }

    @Test
    void createsAndReadsEverySupportedPriorityAsString() {
        for (Priority priority : Priority.values()) {
            Task task = new Task("Task " + priority, Date.valueOf("2024-01-15"), priority);

            taskService.addTask(task);

            Task persisted = taskService.findTaskById(task.getId());
            assertThat(task.getId()).isPositive();
            assertThat(persisted.getPriority()).isEqualTo(priority);
            assertThat(persisted.getDescription()).isEqualTo("Task " + priority);
            assertThat(persisted.getDate()).isEqualTo(Date.valueOf("2024-01-15"));
        }
    }

    @Test
    void storesPriorityAsCanonicalText() {
        Task task = new Task("Text priority", Date.valueOf("2024-02-20"), Priority.HIGH);

        taskService.addTask(task);

        String storedPriority = jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = ?", String.class, task.getId());
        assertThat(storedPriority).isEqualTo("HIGH");
    }

    @Test
    void fourArgumentUpdateChangesPriorityAndPreservesIdentityAndFields() {
        Date originalDate = Date.valueOf("2024-03-10");
        Task task = new Task("Original description", originalDate, Priority.LOW);
        taskService.addTask(task);
        int taskId = task.getId();

        taskService.updateTask(taskId, "Updated description", originalDate, Priority.HIGH);

        Task updated = taskService.findTaskById(taskId);
        assertThat(updated.getId()).isEqualTo(taskId);
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDate()).isEqualTo(originalDate);
        assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void threeArgumentUpdatePreservesExistingPriority() {
        Date originalDate = Date.valueOf("2024-04-11");
        Task task = new Task("Keep priority", originalDate, Priority.MEDIUM);
        taskService.addTask(task);
        int taskId = task.getId();

        taskService.updateTask(taskId, "Updated without priority", Date.valueOf("2024-04-12"));

        Task updated = taskService.findTaskById(taskId);
        assertThat(updated.getId()).isEqualTo(taskId);
        assertThat(updated.getDescription()).isEqualTo("Updated without priority");
        assertThat(updated.getDate()).isEqualTo(Date.valueOf("2024-04-12"));
        assertThat(updated.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void existingCrudOperationsRemainCompatible() {
        Task task = new Task("CRUD task", Date.valueOf("2024-05-05"), Priority.LOW);

        taskService.addTask(task);

        List<Task> tasks = taskService.findAllTasks();
        assertThat(tasks).extracting(Task::getId).contains(task.getId());
        assertThat(taskService.findTaskById(task.getId()).getDescription())
                .isEqualTo("CRUD task");

        taskService.deleteTask(task.getId());

        assertThat(taskService.findTaskById(task.getId())).isNull();
    }
}
