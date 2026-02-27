package com.sms.repository;

import com.sms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA auto-generates the SQL for this — no query needed
    Optional<User> findByUsername(String username);

    // Check if a username is already taken (useful during registration)
    boolean existsByUsername(String username);
}