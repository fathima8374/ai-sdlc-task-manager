package com.example.todoapp.controller;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TaskController.class)
class TaskControllerBaselineTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void homeLoadsTaskListAndCreateTask() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/todo/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("taskList", "createTask", "priorityOptions"))
                .andExpect(model().attribute("msg", ""));
    }

    @Test
    void addTaskRedirectsAfterValidDescription() throws Exception {
        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Write tests")
                .param("date", "2020-01-01")
                .param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        verify(taskService).addTask(org.mockito.ArgumentMatchers.argThat(
                task -> task.getPriority() == Priority.HIGH));
    }

    @Test
    void addTaskRendersHomeAndDoesNotPersistNumericOnlyDescription() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "12345")
                .param("date", "2020-01-01")
                .param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("msg", "Description contains numbers only!"));

        verify(taskService, never()).addTask(any(Task.class));
    }

    @Test
    void addTaskRendersHomeAndDoesNotPersistShortDescription() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "abcd")
                .param("date", "2020-01-01")
                .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("msg", "Description is less than 5 letters!"));

        verify(taskService, never()).addTask(any(Task.class));
    }

    @Test
    void addTaskRejectsMissingPriorityAndPreservesSubmittedValues() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Write tests")
                        .param("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("msg",
                        "Priority is required. Select High, Medium, or Low."))
                .andExpect(model().attribute("createTask",
                        org.hamcrest.Matchers.hasProperty("description",
                                org.hamcrest.Matchers.equalTo("Write tests"))))
                .andExpect(model().attribute("createTask",
                        org.hamcrest.Matchers.hasProperty("priority",
                                org.hamcrest.Matchers.nullValue())));

        verify(taskService, never()).addTask(any(Task.class));
    }

    @Test
    void addTaskRejectsInvalidPriorityBinding() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Write tests")
                        .param("date", "2020-01-01")
                        .param("priority", "URGENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("msg",
                        "Invalid priority. Select High, Medium, or Low."));

        verify(taskService, never()).addTask(any(Task.class));
    }

    @Test
    void updateTaskUsesPriorityAwareServiceMethod() throws Exception {
        mockMvc.perform(post("/todo/saveUpdate")
                        .param("id", "7")
                        .param("description", "Updated task")
                        .param("date", "2020-01-01")
                        .param("priority", "LOW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        verify(taskService).updateTask(eq(7), eq("Updated task"),
                eq(java.sql.Date.valueOf("2020-01-01")), eq(Priority.LOW));
    }

    @Test
    void updateTaskRejectsMissingPriorityAndPreservesSubmittedValues() throws Exception {
        mockMvc.perform(post("/todo/saveUpdate")
                        .param("id", "7")
                        .param("description", "Updated task")
                        .param("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("updateForm"))
                .andExpect(model().attribute("msg",
                        "Priority is required. Select High, Medium, or Low."))
                .andExpect(model().attribute("updateTask",
                        org.hamcrest.Matchers.hasProperty("description",
                                org.hamcrest.Matchers.equalTo("Updated task"))));

        verify(taskService, never()).updateTask(any(Integer.class), any(String.class),
                any(java.util.Date.class), any(Priority.class));
    }

    @Test
    void updateFormReceivesPriorityOptions() throws Exception {
        when(taskService.findTaskById(7)).thenReturn(
                new Task(7, "Existing task", java.sql.Date.valueOf("2020-01-01"),
                        Priority.MEDIUM));

        mockMvc.perform(get("/todo/updateTask/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("updateForm"))
                .andExpect(model().attributeExists("priorityOptions"))
                .andExpect(model().attribute("updateTask",
                        org.hamcrest.Matchers.hasProperty("priority",
                                org.hamcrest.Matchers.equalTo(Priority.MEDIUM))));
    }

    @Test
    void backHomeRedirectsToHome() throws Exception {
        mockMvc.perform(get("/todo/backHome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));
    }
}
