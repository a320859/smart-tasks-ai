package com.example.InsightEngine.services;

import com.example.InsightEngine.clients.AiClient;
import com.example.InsightEngine.dto.*;
import com.example.InsightEngine.dto.SummarizeAndTagContent;
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

import java.util.ArrayList;
import java.util.Arrays;
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

    @Value("${cosineSimilarityThreshold}")
    double cosineSimilarityThreshold;

    @Value("${embeddingModel}")
    String embeddingModel;

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

            String responseFromAi = aiClient.summarizeAndTag(keyForModel, buildSummarizeAndTagRequest(Prompt + allTagsText + taskRequest.getContent()));
            String[] extractAiResponse = extractSummarizeAndTagText(responseFromAi);
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
            log.warning("AI summarize error");
        }

        try{
            String embeddings = extractEmbedding(aiClient.generateEmbeddings(keyForModel, buildEmbeddingRequest(taskRequest.getContent())));
            task.setEmbeddings(embeddings);
        } catch (Exception e) {
            log.warning("AI embeddings error");
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

    public ResponseEntity<?> findTask(String text, UserDetails userDetails) {
        try {
            double[] requestEmbedding = parseEmbeddings(extractEmbedding(aiClient.generateEmbeddings(keyForModel, buildEmbeddingRequest(text))));
            int userId = userRepository.findIdByUsername(userDetails.getUsername());
            List<Task> allTasks = taskRepository.getTasksByUserId(userId);
            List<Task> similarTask = new ArrayList<>();
            for (Task task : allTasks) {
                if (cosineSimilarity(requestEmbedding, parseEmbeddings(task.getEmbeddings())) > cosineSimilarityThreshold) {
                    similarTask.add(task);
                }
            }
            return ResponseEntity.ok(similarTask);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("AI service error");
        }
    }

    private String[] extractSummarizeAndTagText(String text) throws JsonProcessingException {
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

    private SummarizeAndTagDTO buildSummarizeAndTagRequest(String prompt) {
        Part part = new Part();
        part.setText(prompt);
        SummarizeAndTagContent content = new SummarizeAndTagContent();
        content.setParts(List.of(part));
        SummarizeAndTagDTO summarizeAndTagDTO = new SummarizeAndTagDTO();
        summarizeAndTagDTO.setContents(List.of(content));
        return summarizeAndTagDTO;
    }

    private boolean isTaskOwner(int taskId, UserDetails userDetails) {
        int userIdFromTask = taskRepository.getUserIdByTaskId(taskId);
        int currentUserId = userRepository.findIdByUsername(userDetails.getUsername());
        return userIdFromTask == currentUserId;
    }

    private EmbeddingDTO buildEmbeddingRequest(String text) {
        Part part = new Part();
        part.setText(text);
        EmbeddingContent content = new EmbeddingContent();
        content.setParts(part);
        EmbeddingDTO embeddingDTO = new EmbeddingDTO();
        embeddingDTO.setContent(content);
        embeddingDTO.setModel(embeddingModel);
        return embeddingDTO;
    }

    private String extractEmbedding(String embeddings) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(embeddings);
        String text = root.at("/embedding/values").toString();
        int start = text.indexOf("[");
        int end = text.indexOf("]");
        return text.substring(start + 1, end - 1);
    }

    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double[] parseEmbeddings(String text) {
        return Arrays.stream(text.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
