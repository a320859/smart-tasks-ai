package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM mydb.tasks WHERE user_id = :id")
    List<Task> getTasksByUserId(@Param("id") int id);

    @Query(nativeQuery = true, value = "SELECT user_id FROM mydb.tasks WHERE task_id = :taskId")
    int getUserIdByTaskId(@Param("taskId") int taskId);

    @Query(nativeQuery = true, value =
            "SELECT t.*\n" +
            "FROM tasks t\n" +
            "JOIN task_tags tt ON t.task_id = tt.task_id\n" +
            "WHERE tt.tag_id = :tagId AND t.user_id = :userId;\n")
    List<Task> getTasksByTagId(@Param("tagId") int tagId, @Param("userId") int userId);
}
