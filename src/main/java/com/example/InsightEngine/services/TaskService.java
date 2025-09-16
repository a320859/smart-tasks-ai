package com.example.InsightEngine.services;

import com.example.InsightEngine.clients.AiClient;
import com.example.InsightEngine.dto.ResumeAiDTO;
import com.example.InsightEngine.dto.StatusUpdateRequest;
import com.example.InsightEngine.dto.TaskRequest;
import com.example.InsightEngine.model.Content;
import com.example.InsightEngine.model.Part;
import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.repository.TaskRepository;
import com.example.InsightEngine.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private static final Logger log = Logger.getLogger(TaskService.class.getName());

    @Value("${summaryPrompt}")
    String summaryPrompt;

    @Value("${keyForModel}")
    String keyForModel;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, AiClient aiClient) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
    }

    public ResponseEntity<?> addTask(TaskRequest taskRequest, UserDetails userDetails) {
        int id = userRepository.findIdByUsername(userDetails.getUsername());
        Part part = new Part();
        part.setText(summaryPrompt + taskRequest.getContent());
        Content content = new Content();
        content.setParts(List.of(part));
        ResumeAiDTO resumeAiDTO = new ResumeAiDTO();
        resumeAiDTO.setContents(List.of(content));
        String text = null;
        try {
            String resume = aiClient.getResume(keyForModel, resumeAiDTO);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resume);
            text = root.at("/candidates/0/content/parts/0/text").asText(null);
        } catch (Exception e) {
            log.info("AI service error");
        }

        Task task = new Task();
        task.setName(taskRequest.getName());
        task.setContent(text == null ? taskRequest.getContent() : taskRequest.getContent() + "\n   " + text);
        task.setUser(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
        taskRepository.save(task);
        return ResponseEntity.ok(text == null ? "Task added successfully (without AI summary)" : "Task added successfully");
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
