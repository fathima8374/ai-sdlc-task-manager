package com.example.todoapp.controller;

import com.example.todoapp.domain.Task;
import com.example.todoapp.domain.Priority;
import com.example.todoapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
        Task task = new Task(7, "Read API docs", null, Priority.MEDIUM);
        when(taskService.findTaskById(7)).thenReturn(task);

        mockMvc.perform(get("/api/todo/task/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("Read API docs"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
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
        Task persistedTask = new Task(12, "Create API task", null, Priority.HIGH);
        doAnswer(invocation -> {
            Task submittedTask = invocation.getArgument(0);
            assertEquals(0, submittedTask.getId());
            assertEquals(Priority.HIGH, submittedTask.getPriority());
            submittedTask.setId(12);
            return null;
        }).when(taskService).addTask(any(Task.class));
        when(taskService.findTaskById(12)).thenReturn(persistedTask);

        mockMvc.perform(post("/api/todo/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"description\":\"Create API task\",\"date\":null,\"priority\":\"HIGH\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("Added Successfully!"))
                .andExpect(jsonPath("$.task.description").value("Create API task"))
                .andExpect(jsonPath("$.task.priority").value("HIGH"))
                .andExpect(jsonPath("$.task.id").value(12));

        verify(taskService).addTask(any(Task.class));
    }

    @Test
    void updateDelegatesPathIdAndTaskFields() throws Exception {
        when(taskService.findTaskById(4)).thenReturn(
                new Task(4, "Updated API task", null, Priority.LOW));

        mockMvc.perform(put("/api/todo/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"description\":\"Updated API task\",\"date\":null,\"priority\":\"LOW\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.msg").value("Updated Successfully"))
                .andExpect(jsonPath("$.task.id").value(4))
                .andExpect(jsonPath("$.task.priority").value("LOW"));

        verify(taskService).updateTask(eq(4), eq("Updated API task"), eq(null), eq(Priority.LOW));
    }

    @Test
    void addRejectsMissingPriorityWithFieldSpecificBadRequest() throws Exception {
        mockMvc.perform(post("/api/todo/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Create API task\",\"date\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value("Priority is required."));

        verify(taskService, org.mockito.Mockito.never()).addTask(any(Task.class));
    }

    @Test
    void updateRejectsNullPriorityWithFieldSpecificBadRequest() throws Exception {
        mockMvc.perform(put("/api/todo/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated API task\",\"date\":null,\"priority\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value("Priority is required."));

        verify(taskService, org.mockito.Mockito.never()).updateTask(
                any(Integer.class), any(String.class), any(java.util.Date.class), any(Priority.class));
    }

    @Test
    void malformedPriorityReturnsFieldSpecificBadRequest() throws Exception {
        mockMvc.perform(post("/api/todo/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Create API task\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("priority"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid priority. Use HIGH, MEDIUM, or LOW."));

        verify(taskService, org.mockito.Mockito.never()).addTask(any(Task.class));
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
