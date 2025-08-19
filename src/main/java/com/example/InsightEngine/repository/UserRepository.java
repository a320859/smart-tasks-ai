package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {
}
