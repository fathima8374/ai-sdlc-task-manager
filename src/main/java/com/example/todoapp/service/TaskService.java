package com.example.todoapp.service;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;

import java.util.Date;
import java.util.List;

public interface TaskService {
    void addTask(Task task);
    void deleteTask(int id);
    List<Task> findAllTasks();
    Task findTaskById(int id);
    /**
     * Retained for existing controllers until they pass priority explicitly.
     * The stored priority is preserved; no default is assigned.
     */
    void updateTask(int id, String description, Date date);
    void updateTask(int id, String description, Date date, Priority priority);
    }
