package com.example.todoapp.controller;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TaskController.class)
class TaskPriorityTemplateRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void createTemplateRendersEmptyPriorityAndAllOptions() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/todo/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"\" disabled selected=\"selected\">Select priority</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"HIGH\">HIGH</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"MEDIUM\">MEDIUM</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"LOW\">LOW</option>")));
    }

    @Test
    void updateTemplatePreselectsExistingPriorityAndRendersNullSafely() throws Exception {
        when(taskService.findTaskById(7)).thenReturn(
                new Task(7, "Existing task", Date.valueOf("2020-01-01"), Priority.MEDIUM));

        mockMvc.perform(get("/todo/updateTask/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("updateForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"MEDIUM\" selected=\"selected\">MEDIUM</option>")));
    }

    @Test
    void createValidationRendersSubmittedValuesAndNullPrioritySafely() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.singletonList(
                new Task(1, "Legacy task", Date.valueOf("2020-01-01"))));

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Submitted task")
                        .param("date", "2020-02-02")
                        .param("priority", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Priority is required. Select High, Medium, or Low.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "value=\"Submitted task\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "value=\"2020-02-02\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Not set")));
    }
}
