package com.example.todoapp.controller;

import com.example.todoapp.dao.TaskRepository;
import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import com.example.todoapp.migration.TaskPriorityMigration;
import org.springframework.boot.DefaultApplicationArguments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TaskPriorityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskPriorityMigration migration;

    @BeforeEach
    void clearTasks() {
        taskRepository.deleteAll();
    }

    @Test
    void webCreatePersistsHighMediumAndLow() throws Exception {
        for (Priority priority : Priority.values()) {
            mockMvc.perform(post("/todo/addTask")
                            .param("description", "Web " + priority)
                            .param("date", "2024-06-01")
                            .param("priority", priority.name()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/todo/home"));
        }

        Iterable<Task> tasks = taskRepository.findAll();
        assertThat(tasks).extracting(Task::getPriority)
                .containsExactlyInAnyOrder(Priority.HIGH, Priority.MEDIUM, Priority.LOW);
        assertThat(tasks).allSatisfy(task -> assertThat(task.getId()).isPositive());
    }

    @Test
    void webUpdateChangesPriorityAndPreservesIdDescriptionAndDate() throws Exception {
        Task original = saveTask("Original web task", "2024-06-02", Priority.LOW);

        mockMvc.perform(post("/todo/saveUpdate")
                        .param("id", String.valueOf(original.getId()))
                        .param("description", "Updated web task")
                        .param("date", "2024-06-03")
                        .param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        Task updated = taskRepository.findById(original.getId()).orElseThrow();
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getDescription()).isEqualTo("Updated web task");
        assertThat(updated.getDate()).isEqualTo(Date.valueOf("2024-06-03"));
        assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void webRejectsMissingPriorityWithoutPersistenceAndPreservesSubmittedValues() throws Exception {
        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Preserved web task")
                        .param("date", "2024-06-04")
                        .param("priority", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("createTask",
                        org.hamcrest.Matchers.hasProperty("description",
                                org.hamcrest.Matchers.equalTo("Preserved web task"))))
                .andExpect(model().attribute("createTask",
                        org.hamcrest.Matchers.hasProperty("priority",
                                org.hamcrest.Matchers.nullValue())));

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void webRejectsInvalidDescriptionsWithoutPersistence() throws Exception {
        mockMvc.perform(post("/todo/addTask")
                        .param("description", "12345")
                        .param("date", "2024-06-05")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("msg", "Description contains numbers only!"));

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "abcd")
                        .param("date", "2024-06-05")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("msg", "Description is less than 5 letters!"));

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void webFormsRenderPriorityOptionsAndCurrentValue() throws Exception {
        Task task = saveTask("Form task", "2024-06-06", Priority.MEDIUM);

        mockMvc.perform(get("/todo/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"HIGH\">HIGH</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<td>MEDIUM</td>")));

        mockMvc.perform(get("/todo/updateTask/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("updateForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"MEDIUM\" selected=\"selected\">MEDIUM</option>")));
    }

    @Test
    void webListPreservesRepositoryOrderWithoutPrioritySorting() throws Exception {
        Task first = saveTask("First order task", "2024-06-07", Priority.LOW);
        Task second = saveTask("Second order task", "2024-06-08", Priority.HIGH);

        String html = mockMvc.perform(get("/todo/home"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html.indexOf("First order task"))
                .isLessThan(html.indexOf("Second order task"));
        assertThat(first.getId()).isLessThan(second.getId());
    }

    @Test
    void restCreatePersistsEveryPriorityAndReturnsPersistedTask() throws Exception {
        for (Priority priority : Priority.values()) {
            mockMvc.perform(post("/api/todo/add")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content("{\"id\":999,\"description\":\"REST " + priority
                                    + "\",\"date\":\"2024-06-09\",\"priority\":\""
                                    + priority.name() + "\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.task.id").isNumber())
                    .andExpect(jsonPath("$.task.id").value(org.hamcrest.Matchers.greaterThan(0)))
                    .andExpect(jsonPath("$.task.priority").value(priority.name()));
        }

        assertThat(taskRepository.findAll()).extracting(Task::getPriority)
                .containsExactlyInAnyOrder(Priority.HIGH, Priority.MEDIUM, Priority.LOW);
        assertThat(taskRepository.findAll()).allSatisfy(task ->
                assertThat(task.getDescription()).startsWith("REST "));
    }

    @Test
    void restUpdateUsesPathIdAndReturnsPersistedPriorityAndFields() throws Exception {
        Task original = saveTask("Original REST task", "2024-06-10", Priority.MEDIUM);

        mockMvc.perform(put("/api/todo/update/" + original.getId())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"description\":\"Updated REST task\","
                                + "\"date\":\"2024-06-11\",\"priority\":\"LOW\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.task.id").value(original.getId()))
                .andExpect(jsonPath("$.task.description").value("Updated REST task"))
                .andExpect(jsonPath("$.task.priority").value("LOW"));

        Task updated = taskRepository.findById(original.getId()).orElseThrow();
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getDate()).isEqualTo(Date.valueOf("2024-06-11"));
        assertThat(taskRepository.findById(999)).isEmpty();
    }

    @Test
    void restInvalidAndMissingPriorityDoNotPersist() throws Exception {
        mockMvc.perform(post("/api/todo/add")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Missing REST priority\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value("Priority is required."));

        mockMvc.perform(put("/api/todo/update/123")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Null REST priority\",\"priority\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value("Priority is required."));

        mockMvc.perform(post("/api/todo/add")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Invalid REST priority\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid priority. Use HIGH, MEDIUM, or LOW."));

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void malformedJsonCurrentlyUsesPriorityErrorClassification() throws Exception {
        mockMvc.perform(post("/api/todo/add")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Malformed JSON\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid priority. Use HIGH, MEDIUM, or LOW."));

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void restListAndDeleteRemainCompatible() throws Exception {
        Task first = saveTask("REST first", "2024-06-12", Priority.HIGH);
        Task second = saveTask("REST second", "2024-06-13", Priority.LOW);

        mockMvc.perform(get("/api/todo/tasksList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.getId()))
                .andExpect(jsonPath("$[1].id").value(second.getId()))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[1].priority").value("LOW"));

        mockMvc.perform(delete("/api/todo/delete/" + first.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(first.getId()))
                .andExpect(jsonPath("$.msg").value("Deleted Successfully"));

        assertThat(taskRepository.findById(first.getId())).isEmpty();
        assertThat(taskRepository.findById(second.getId())).isPresent();
    }

    @Test
    void legacyNullPriorityIsBackfilledWithoutOverwritingValidValues() {
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (200, '2024-06-14', 'Legacy integration task', NULL)");
        jdbcTemplate.update(
                "INSERT INTO task (id, date, description, priority) " +
                        "VALUES (201, '2024-06-15', 'Valid integration task', 'HIGH')");

        migration.run(new DefaultApplicationArguments());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = 200", String.class))
                .isEqualTo("MEDIUM");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = 201", String.class))
                .isEqualTo("HIGH");
    }

    private Task saveTask(String description, String date, Priority priority) {
        Task task = new Task(description, Date.valueOf(date), priority);
        taskRepository.save(task);
        return task;
    }
}
