package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM users WHERE username = :username")
    int countOfUsersWithUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO users (username, password, enabled) VALUES (:username, :password, 1)")
    void addUser(@Param("username") String username, @Param("password") String password);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO authorities (username, authority) VALUES (:username, 'write')")
    void addUserAuthority(@Param("username") String username);

    @Query(nativeQuery = true, value = "SELECT user_id FROM users WHERE username = :username")
    int findIdByUsername(@Param("username") String username);
}
