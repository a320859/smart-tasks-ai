package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagsRepository extends JpaRepository<Tags, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM tags")
    List<Tags> getTags();

    @Query(nativeQuery = true, value = "SELECT * FROM tags WHERE name = :name")
    Tags getTagByName(String name);
}
