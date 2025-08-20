package com.example.InsightEngine.dto;

import com.example.InsightEngine.enums.TaskStatuses;

public class StatusUpdateRequest {
    private TaskStatuses status;

    public TaskStatuses getStatus() {
        return status;
    }

    public void setStatus(TaskStatuses status) {
        this.status = status;
    }
}
