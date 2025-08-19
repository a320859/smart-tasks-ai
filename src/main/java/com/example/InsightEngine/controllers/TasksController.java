package com.example.InsightEngine.controllers;

import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.repository.TaskRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TasksController {
    private final TaskRepository taskRepository;

    public TasksController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping("/tasks")
    public void addTask(@RequestBody Task task) {
        taskRepository.save(task);
    }

    @GetMapping("/tasks")
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(@PathVariable Integer taskId) {
        taskRepository.deleteById(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public void changeTask(@PathVariable int taskId, @RequestBody Task updateTask) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found!"));
        task.setName(updateTask.getName());
        task.setContent(updateTask.getContent());
        taskRepository.save(task);
    }
}
