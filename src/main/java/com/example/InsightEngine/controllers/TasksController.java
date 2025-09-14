package com.example.InsightEngine.controllers;

import com.example.InsightEngine.dto.StatusUpdateRequest;
import com.example.InsightEngine.dto.TaskRequest;
import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TasksController {
    private final TaskService taskService;

    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }


    @PostMapping("/tasks")
    public ResponseEntity<?> addTask(@RequestBody TaskRequest taskRequest,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.addTask(taskRequest, userDetails);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        return taskService.getTasks(userDetails.getUsername());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.deleteTask(taskId, userDetails);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> changeTask(@PathVariable int taskId, @RequestBody Task updateTask,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.changeTask(taskId, updateTask, userDetails);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable int taskId, @RequestBody StatusUpdateRequest newStatus,
                             @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.changeStatus(taskId, newStatus, userDetails);
    }
}
