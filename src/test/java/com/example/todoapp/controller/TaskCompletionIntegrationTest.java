package com.example.todoapp.controller;

import com.example.todoapp.dao.TaskRepository;
import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TaskCompletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clearTasks() {
        taskRepository.deleteAll();
    }

    @Test
    void defaultsToPendingAndPersistsBothTransitionsWithoutChangingOtherFields()
            throws Exception {
        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Keep task fields")
                        .param("date", "2024-07-01")
                        .param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection());

        Task task = taskRepository.findAll().iterator().next();

        Task created = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(created.getCompleted()).isFalse();

        mockMvc.perform(post("/todo/setCompletion/" + task.getId())
                        .param("completed", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        Task completed = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(completed.getCompleted()).isTrue();
        assertThat(completed.getId()).isEqualTo(task.getId());
        assertThat(completed.getDescription()).isEqualTo("Keep task fields");
        assertThat(completed.getDate()).isEqualTo(Date.valueOf("2024-07-01"));
        assertThat(completed.getPriority()).isEqualTo(Priority.HIGH);

        mockMvc.perform(post("/todo/setCompletion/" + task.getId())
                        .param("completed", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        Task pending = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(pending.getCompleted()).isFalse();
        assertThat(pending.getDescription()).isEqualTo("Keep task fields");
        assertThat(pending.getDate()).isEqualTo(Date.valueOf("2024-07-01"));
        assertThat(pending.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void taskListDisplaysEachStatusAndCorrespondingAction() throws Exception {
        Task pending = new Task("Pending task", Date.valueOf("2024-07-02"), Priority.LOW);
        Task completed = new Task("Completed task", Date.valueOf("2024-07-03"), Priority.MEDIUM);
        completed.setCompleted(true);
        taskRepository.save(pending);
        taskRepository.save(completed);

        mockMvc.perform(get("/todo/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<span>Pending</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<span>Completed</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "value=\"true\">Mark Completed")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "value=\"false\">Mark Pending")));
    }

    @Test
    void ordinaryTaskEditPreservesCompletionStatus() throws Exception {
        Task task = new Task("Original description", Date.valueOf("2024-07-04"), Priority.LOW);
        task.setCompleted(true);
        taskRepository.save(task);

        mockMvc.perform(post("/todo/saveUpdate")
                        .param("id", String.valueOf(task.getId()))
                        .param("description", "Updated description")
                        .param("date", "2024-07-05")
                        .param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection());

        Task updated = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updated.getCompleted()).isTrue();
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDate()).isEqualTo(Date.valueOf("2024-07-05"));
        assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void deletingTaskStillRemovesIt() throws Exception {
        Task task = new Task("Delete completed task", Date.valueOf("2024-07-06"), Priority.MEDIUM);
        task.setCompleted(true);
        taskRepository.save(task);

        mockMvc.perform(get("/todo/deleteTask/" + task.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}
