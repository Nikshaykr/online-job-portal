package com.jobportal.repository;

import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for User entity.
 * Spring Data JPA auto-implements CRUD + the custom methods below.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their email address (used for login)
    Optional<User> findByEmail(String email);

    // Check if an email is already registered (used in register)
    boolean existsByEmail(String email);
}
