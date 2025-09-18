package com.example.InsightEngine.dto;

import java.util.List;

public class SummarizeAndTagDTO {
    private List<SummarizeAndTagContent> contents;

    public List<SummarizeAndTagContent> getContents() {
        return contents;
    }

    public void setContents(List<SummarizeAndTagContent> contents) {
        this.contents = contents;
    }
}
