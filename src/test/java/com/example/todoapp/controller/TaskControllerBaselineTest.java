package com.example.todoapp.controller;

import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
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
                .andExpect(model().attributeExists("taskList", "createTask"))
                .andExpect(model().attribute("msg", ""));
    }

    @Test
    void addTaskRedirectsAfterValidDescription() throws Exception {
        mockMvc.perform(post("/todo/addTask")
                        .param("description", "Write tests")
                        .param("date", "2020-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));

        verify(taskService).addTask(any(Task.class));
    }

    @Test
    void addTaskRendersHomeAndDoesNotPersistNumericOnlyDescription() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/todo/addTask")
                        .param("description", "12345")
                        .param("date", "2020-01-01"))
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
                        .param("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("msg", "Description is less than 5 letters!"));

        verify(taskService, never()).addTask(any(Task.class));
    }

    @Test
    void backHomeRedirectsToHome() throws Exception {
        mockMvc.perform(get("/todo/backHome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todo/home"));
    }
}
