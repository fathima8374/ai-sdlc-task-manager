package com.example.todoapp.controller;

import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskControllerREST.class)
class TaskControllerRESTBaselineTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void findTaskByIdReturnsTask() throws Exception {
        Task task = new Task(7, "Read API docs", null);
        when(taskService.findTaskById(7)).thenReturn(task);

        mockMvc.perform(get("/api/todo/task/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("Read API docs"));
    }

    @Test
    void findAllTasksReturnsList() throws Exception {
        when(taskService.findAllTasks()).thenReturn(Collections.singletonList(
                new Task(1, "First task", null)));

        mockMvc.perform(get("/api/todo/tasksList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("First task"));
    }

    @Test
    void addReturnsCreatedResponseAndDelegatesTask() throws Exception {
        mockMvc.perform(post("/api/todo/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Create API task\",\"date\":null}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("Added Successfully!"))
                .andExpect(jsonPath("$.task.description").value("Create API task"));

        verify(taskService).addTask(any(Task.class));
    }

    @Test
    void updateDelegatesPathIdAndTaskFields() throws Exception {
        mockMvc.perform(put("/api/todo/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated API task\",\"date\":null}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.msg").value("Updated Successfully"));

        verify(taskService).updateTask(eq(4), eq("Updated API task"), eq(null));
    }

    @Test
    void deleteReturnsAcceptedResponse() throws Exception {
        mockMvc.perform(delete("/api/todo/delete/9"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.msg").value("Deleted Successfully"));

        verify(taskService).deleteTask(9);
    }
}
