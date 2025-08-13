package com.adamo.vrspfab.users;

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


    /**
     * Checks if a user exists by their phone number.
     *
     * @param phoneNumber the phone number of the user
     * @return true if a user with the given phone number exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Finds a user by their reset token.
     *
     * @param resetToken the reset token
     * @return an Optional containing the User if found, or empty if not found
     */
    Optional<User> findByResetToken(String resetToken);


    /**
     * Checks if a user exists by their phone number, excluding a specific user ID.
     *
     * @param phoneNumber the phone number of the user
     * @param userId the ID of the user to exclude from the check
     * @return true if a user with the given phone number exists and is not the specified user, false otherwise
     */
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long userId);
}
