package com.example.todoapp.controller;

import com.example.todoapp.domain.Priority;
import com.example.todoapp.domain.Task;
import com.example.todoapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@RequestMapping("/todo")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/addTask")
    public String addTask(Task task, BindingResult bindingResult, Model model) {
        addPriorityOptions(model);

        if (bindingResult.hasFieldErrors("priority")) {
            return renderCreateValidationError(task, model,
                    "Invalid priority. Select High, Medium, or Low.");
        }

        if (task.getPriority() == null) {
            return renderCreateValidationError(task, model,
                    "Priority is required. Select High, Medium, or Low.");
        }

        if (ifOnlyNumbers(task.getDescription())) {
            return renderCreateValidationError(task, model,
                    "Description contains numbers only!");
        }

        if(DescriptionIsLessThanThreeFiveLetters(task.getDescription()) ){
            return renderCreateValidationError(task, model,
                    "Description is less than 5 letters!");
        }


        taskService.addTask(task);
        return "redirect:/todo/home";
    }

    @GetMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable int id) {
        taskService.deleteTask(id);
        return "redirect:/todo/home";
    }

    @GetMapping("/home")
    public String findAllTasks(Model model) {
        List<Task> taskList = taskService.findAllTasks();
        model.addAttribute("msg","");
        model.addAttribute("taskList", taskList);
        model.addAttribute("createTask",new Task());
        addPriorityOptions(model);
        return "home";
    }


    @GetMapping("/updateTask/{id}")
    public String updateTask (@PathVariable int id, Model model){
        Task task = taskService.findTaskById(id);
        model.addAttribute("updateTask",task);
        addPriorityOptions(model);
        return "updateForm";
    }

    @PostMapping("/saveUpdate")
    public String saveUpdate (Task task, BindingResult bindingResult, Model model){
        addPriorityOptions(model);

        if (bindingResult.hasFieldErrors("priority")) {
            return renderUpdateValidationError(task, model,
                    "Invalid priority. Select High, Medium, or Low.");
        }

        if (task.getPriority() == null) {
            return renderUpdateValidationError(task, model,
                    "Priority is required. Select High, Medium, or Low.");
        }

        if (ifOnlyNumbers(task.getDescription())) {
            return renderUpdateValidationError(task, model,
                    "Description contains numbers only! Please, Try again!");
        }

        if(DescriptionIsLessThanThreeFiveLetters(task.getDescription()) ){
            return renderUpdateValidationError(task, model,
                    "Description is less than 5 letters! Please, Try again!");
        }

        taskService.updateTask(task.getId(),task.getDescription(),task.getDate(),
                task.getPriority());
        return "redirect:/todo/home";
    }

    @GetMapping("/backHome")
    public String backHome (){
        return "redirect:/todo/home";
    }

    public boolean DescriptionIsLessThanThreeFiveLetters(String Description){
        String str = Description.trim();
        System.out.println(str.length());
        if (str.length() < 5)
            return true;

        return false;
    }

    public boolean ifOnlyNumbers(String Description){
                boolean numeric = true;
                try {
                    Double num = Double.parseDouble(Description);
                } catch (NumberFormatException e) {
                    numeric = false;
                }

                if(numeric)
                    return true;
                else
                    return false;
    }

    private String renderCreateValidationError(Task task, Model model, String message) {
        model.addAttribute("msg", message);
        model.addAttribute("priorityError", message.startsWith("Priority")
                || message.startsWith("Invalid priority"));
        model.addAttribute("taskList", taskService.findAllTasks());
        model.addAttribute("createTask", task);
        return "home";
    }

    private String renderUpdateValidationError(Task task, Model model, String message) {
        model.addAttribute("msg", message);
        model.addAttribute("priorityError", message.startsWith("Priority")
                || message.startsWith("Invalid priority"));
        model.addAttribute("updateTask", task);
        return "updateForm";
    }

    private void addPriorityOptions(Model model) {
        model.addAttribute("priorityOptions", Priority.values());
    }

}
