package com.example.InsightEngine.services;

import com.example.InsightEngine.dto.StatusUpdateRequest;
import com.example.InsightEngine.dto.TaskRequest;
import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.repository.TaskRepository;
import com.example.InsightEngine.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> addTask(TaskRequest taskRequest, UserDetails userDetails) {
        int id = userRepository.findIdByUsername(userDetails.getUsername());
        Task task = new Task();
        task.setName(taskRequest.getName());
        task.setContent(taskRequest.getContent());
        task.setUser(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
        taskRepository.save(task);
        return ResponseEntity.ok("Task added successfully");
    }

    public ResponseEntity<List<Task>> getTasks(String username) {
        int id = userRepository.findIdByUsername(username);
        return ResponseEntity.ok().body(taskRepository.getTasksByUserId(id));
    }

    public ResponseEntity<?> deleteTask(Integer taskId, UserDetails userDetails) {
        int userId = userRepository.findIdByUsername(userDetails.getUsername());
        int userIdFromTask = taskRepository.getUserIdByTaskId(taskId);
        if (userIdFromTask == userId) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.ok("Deletion was successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
    }

    public ResponseEntity<?> changeTask (int taskId, Task updateTask, UserDetails userDetails) {
        int userId = userRepository.findIdByUsername(userDetails.getUsername());
        int userIdFromTask = taskRepository.getUserIdByTaskId(taskId);
        if (userIdFromTask == userId) {
            Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
            task.setName(updateTask.getName());
            task.setContent(updateTask.getContent());
            taskRepository.save(task);
            return ResponseEntity.ok("Task changed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
    }

    public ResponseEntity<?> changeStatus(int taskId, StatusUpdateRequest statusUpdateRequest,
                                          UserDetails userDetails) {
        int userId = userRepository.findIdByUsername(userDetails.getUsername());
        int userIdFromTask = taskRepository.getUserIdByTaskId(taskId);
        if (userIdFromTask == userId) {
            Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
            task.setStatus(statusUpdateRequest.getStatus());
            taskRepository.save(task);
            return ResponseEntity.ok("Status changed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
    }
}
