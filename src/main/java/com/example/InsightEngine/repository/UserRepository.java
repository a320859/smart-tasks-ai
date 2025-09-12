package com.example.InsightEngine.repository;

import com.example.InsightEngine.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM mydb.users WHERE username = :username")
    int countOfUsersWithUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO mydb.users (username, password) VALUES (:username, :password)")
    void addUser(@Param("username") String username, @Param("password") String password);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO mydb.authorities (username, authority) VALUES (:username, 'write')")
    void addUserAuthority(@Param("username") String username);
}
