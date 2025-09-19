package com.example.InsightEngine.dto;

public class EmbeddingDTO {
    private String model;
    private EmbeddingContent content;

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setContent(EmbeddingContent content) {
        this.content = content;
    }

    public EmbeddingContent getContent() {
        return content;
    }
}
