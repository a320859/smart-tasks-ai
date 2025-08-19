package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {
}
