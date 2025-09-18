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

    @Value("${prompt}")
    String Prompt;

    @Value("${keyForModel}")
    String keyForModel;

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
        Task task = new Task();
        try {
            List<Tags> allTags = tagsRepository.getTags();
            String allTagsText = allTags.stream()
                    .map(Tags::getName)
                    .collect(Collectors.joining());

            String responseFromAi = aiClient.summarizeAndTag(keyForModel, buildRequest(Prompt + allTagsText + taskRequest.getContent()));
            String[] extractAiResponse = extractText(responseFromAi);
            log.info("Tags from AI: " + extractAiResponse[1]);
            for (String tagName: extractAiResponse[1].split(",")) {
                tagName = tagName.trim();
                Tags tag = tagsRepository.getTagByName(tagName);
                if (tag == null){
                    tag = new Tags();
                    tag.setName(tagName);
                    tagsRepository.save(tag);
                }
                task.getTags().add(tag);
            }
            textResume = extractAiResponse[0];
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
        if (isTaskOwner(taskId, userDetails)) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.ok("Deletion was successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
    }

    public ResponseEntity<?> changeTask (int taskId, Task updateTask, UserDetails userDetails) {
        if (isTaskOwner(taskId, userDetails)) {
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
        if (isTaskOwner(taskId, userDetails)) {
            Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
            task.setStatus(statusUpdateRequest.getStatus());
            taskRepository.save(task);
            return ResponseEntity.ok("Status changed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
    }

    private String[] extractText(String text) throws JsonProcessingException {
        String[] e = new String[2];
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(text);
        String myText = root.at("/candidates/0/content/parts/0/text").asText(null);
        int start = myText.indexOf("{");
        int end = myText.indexOf("}");
        myText = myText.substring(start, end + 1);
        root = mapper.readTree(myText);

        String summary = root.at("/summary").asText(null);
        String tags = root.at("/tags").asText(null);
        e[0] = summary;
        e[1] = tags;
        return e;
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

    private boolean isTaskOwner(int taskId, UserDetails userDetails) {
        int userIdFromTask = taskRepository.getUserIdByTaskId(taskId);
        int currentUserId = userRepository.findIdByUsername(userDetails.getUsername());
        return userIdFromTask == currentUserId;
    }
}
