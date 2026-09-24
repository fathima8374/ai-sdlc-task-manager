package com.example.todoapp.dao;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;

import java.util.Date;
import java.util.List;

public interface TaskDao {
    void addTask(Task task);
    void deleteTask(int id);
    List<Task> findAllTasks();
    Task findTaskById(int id);
    /**
     * Retained for existing callers until web and REST controllers pass priority.
     * The implementation preserves the stored priority and does not assign a default.
     */
    void updateTask(int id, String description, Date date);
    void updateTask(int id, String description, Date date, Priority priority);
}
