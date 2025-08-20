package com.example.InsightEngine.controllers;

import com.example.InsightEngine.dto.StatusUpdateRequest;
import com.example.InsightEngine.dto.TaskRequest;
import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.repository.TaskRepository;
import com.example.InsightEngine.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TasksController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TasksController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }


    @PostMapping("/tasks")
    public void addTask(@RequestBody TaskRequest taskRequest) {
        Task task = new Task();
        task.setUser(userRepository.findById(taskRequest.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
        task.setName(taskRequest.getName());
        task.setContent(taskRequest.getContent());
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

    @PatchMapping("/tasks/{taskId}/status")
    public void changeStatus(@PathVariable int taskId, @RequestBody StatusUpdateRequest newStatus) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(newStatus.getStatus());
        taskRepository.save(task);
    }
}
