package com.example.InsightEngine.clients;

import com.example.InsightEngine.dto.EmbeddingDTO;
import com.example.InsightEngine.dto.SummarizeAndTagDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ai", url = "https://generativelanguage.googleapis.com/v1beta/models")
public interface AiClient {

    @PostMapping("/gemini-2.5-flash:generateContent")
    String summarizeAndTag(@RequestHeader(name = "x-goog-api-key") String key, @RequestBody SummarizeAndTagDTO summarizeAndTagDTO);

    @PostMapping("/gemini-embedding-001:embedContent")
    String generateEmbeddings(@RequestHeader(name = "x-goog-api-key") String key, @RequestBody EmbeddingDTO embeddingDTO);
}
