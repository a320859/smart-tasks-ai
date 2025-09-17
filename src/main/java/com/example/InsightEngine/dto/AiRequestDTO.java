package com.example.InsightEngine.dto;

import java.util.List;

public class AiRequestDTO {
    private List<Content> contents;

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}
