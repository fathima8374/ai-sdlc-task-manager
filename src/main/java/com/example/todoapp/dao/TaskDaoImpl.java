package com.example.todoapp.dao;

import com.example.todoapp.domain.Task;
import com.example.todoapp.domain.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskDaoImpl implements TaskDao {

    @Autowired
    TaskRepository taskRepository;

    @Override
    public void addTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void deleteTask(int id) {
        taskRepository.deleteById(id);
    }


    @Override
    public List<Task> findAllTasks() {
        Iterable<Task> taskIterable = taskRepository.findAll();

        List<Task> taskList = new ArrayList<>();
        if (taskList != null) {
            for (Task task : taskIterable) {
                taskList.add(task);
            }
            return taskList;
        } else {
            return null;

        }
    }

    @Override
    public Task findTaskById(int id) {

        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            return task.get();
        } else {
            return null;
        }

    }

    @Override
    public void updateTask(int id, String description, Date date) {
        updateExistingTask(id, description, date, null, false);
    }

    @Override
    public void updateTask(int id, String description, Date date, Priority priority) {
        updateExistingTask(id, description, date, priority, true);
    }

    private void updateExistingTask(int id, String description, Date date,
                                    Priority priority, boolean updatePriority) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setDate(date);
            task.setDescription(description);
            if (updatePriority) {
                task.setPriority(priority);
            }
            taskRepository.save(task);
        }
    }
}
