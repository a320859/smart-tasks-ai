package com.example.InsightEngine.services;

import com.example.InsightEngine.clients.AiClient;
import com.example.InsightEngine.dto.*;
import com.example.InsightEngine.dto.Content;
import com.example.InsightEngine.model.Tags;
import com.example.InsightEngine.model.Task;
import com.example.InsightEngine.repository.TagsRepository;
import com.example.InsightEngine.repository.TaskRepository;
import com.example.InsightEngine.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TagsRepository tagsRepository;
    private final AiClient aiClient;
    private static final Logger log = Logger.getLogger(TaskService.class.getName());

    @Value("${summaryPrompt}")
    String summaryPrompt;

    @Value("${keyForModel}")
    String keyForModel;

    @Value("${tagsPrompt}")
    String tagsPrompt;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       AiClient aiClient, TagsRepository tagsRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
        this.tagsRepository = tagsRepository;
    }

    public ResponseEntity<?> addTask(TaskRequest taskRequest, UserDetails userDetails) {
        int id = userRepository.findIdByUsername(userDetails.getUsername());

        String textResume = null;
        try {
            String resume = aiClient.getResume(keyForModel, buildRequest(summaryPrompt + taskRequest.getContent()));
            textResume = extractText(resume);
        } catch (Exception e) {
            log.info("AI service error");
        }

        String textTags = null;
        Task task = new Task();
        try {
            List<Tags> allTags = tagsRepository.getTags();
            String allTagsText = allTags.stream()
                    .map(Tags::getName)
                    .collect(Collectors.joining());

            String tagsFromAi = aiClient.getResume(keyForModel, buildRequest(tagsPrompt + allTagsText + taskRequest.getContent()));

            textTags = extractText(tagsFromAi);
            log.info("Tags from AI: " + textTags);
            for (String tagName: textTags.split(",")) {
                tagName = tagName.trim();
                Tags tag = tagsRepository.getTagByName(tagName);
                if (tag == null){
                    tag = new Tags();
                    tag.setName(tagName);
                    tagsRepository.save(tag);
                }
                task.getTags().add(tag);
            }
        } catch (Exception e) {
            log.warning("AI service error");
        }

        task.setName(taskRequest.getName());
        task.setContent(textResume == null ? taskRequest.getContent() : taskRequest.getContent() + "\n   " + textResume);
        task.setUser(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));

        taskRepository.save(task);
        return ResponseEntity.ok(textResume == null ? "Task added successfully (without AI summary)" : "Task added successfully");
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

    private String extractText(String text) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(text);
        return root.at("/candidates/0/content/parts/0/text").asText(null);
    }

    private AiRequestDTO buildRequest(String prompt) {
        Part part = new Part();
        part.setText(prompt);
        Content content = new Content();
        content.setParts(List.of(part));
        AiRequestDTO aiRequestDTO = new AiRequestDTO();
        aiRequestDTO.setContents(List.of(content));
        return aiRequestDTO;
    }
}
