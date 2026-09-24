package com.example.todoapp.controller;

import com.example.todoapp.domain.Task;
import com.example.todoapp.domain.Priority;
import com.example.todoapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todo")
public class TaskControllerREST {

    @Autowired
    TaskService taskService;

    @GetMapping("/task/{id}")
    public Task findTaskById(@PathVariable int id) {
        Task task = taskService.findTaskById(id);
        return task;
    }

    @GetMapping("/tasksList")
    public List<Task> findAllTasks() {
        return taskService.findAllTasks();
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public HashMap<String, Object> add(@RequestBody Task task) {
        if (task == null || task.getPriority() == null) {
            throw new InvalidPriorityException("Priority is required.");
        }

        task.setId(0);
        HashMap<String, Object> response = new HashMap<>();
        taskService.addTask(task);
        response.put("task", taskService.findTaskById(task.getId()));
        response.put("msg", "Added Successfully!");
        return response;
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String,Object> delete(@PathVariable int id) {
        HashMap<String,Object> response = new HashMap<>();
        taskService.deleteTask(id);
        response.put("id",id);
        response.put("msg","Deleted Successfully");
        return response;
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String,Object> update(@PathVariable int id,@RequestBody Task task) {
        if (task == null || task.getPriority() == null) {
            throw new InvalidPriorityException("Priority is required.");
        }

        task.setId(id);
        HashMap<String,Object> response = new HashMap<>();
        taskService.updateTask(id,task.getDescription(),task.getDate(),task.getPriority());
        response.put("task", taskService.findTaskById(id));
        response.put("msg","Updated Successfully");
        return response;
    }

    @ExceptionHandler(InvalidPriorityException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPriority(
            InvalidPriorityException exception) {
        return priorityError(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableRequest(
            HttpMessageNotReadableException exception) {
        return priorityError("Invalid priority. Use HIGH, MEDIUM, or LOW.");
    }

    private ResponseEntity<Map<String, String>> priorityError(String message) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("field", "priority");
        error.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private static class InvalidPriorityException extends RuntimeException {
        private InvalidPriorityException(String message) {
            super(message);
        }
    }

}
