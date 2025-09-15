package com.example.InsightEngine.dto;

import com.example.InsightEngine.model.Content;

import java.util.List;

public class ResumeAiDTO {
    private List<Content> contents;

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}
