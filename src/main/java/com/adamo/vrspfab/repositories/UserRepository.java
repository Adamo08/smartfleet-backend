package com.adamo.vrspfab.repositories;

import com.adamo.vrspfab.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user
     * @return an Optional containing the User if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by their email address.
     *
     * @param email the email address of the user
     * @return true if a user with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
