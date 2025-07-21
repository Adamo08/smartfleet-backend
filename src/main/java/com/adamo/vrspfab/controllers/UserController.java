package com.adamo.vrspfab.controllers;

import com.adamo.vrspfab.dtos.ChangeUserPasswordRequest;
import com.adamo.vrspfab.dtos.RegisterUserRequest;
import com.adamo.vrspfab.dtos.UpdateUserRequest;
import com.adamo.vrspfab.dtos.UserDto;
import com.adamo.vrspfab.entities.User;
import com.adamo.vrspfab.entities.enums.Role;
import com.adamo.vrspfab.mappers.UserMapper;
import com.adamo.vrspfab.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Manage Users", description = "Operations related to user management")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    /**
     * This method handles requests to the "/users" URL and returns a list of users.
     *
     * @return A list of User objects.
     */
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all registered users, sorted by name or email."
    )
    public List<UserDto> getAllUsers(@RequestParam (name = "sort") String sort){

        if (!Set.of("name", "email").contains(sort)) {
            sort = "name";
        }

        return userRepository.findAll(Sort.by(Sort.Direction.ASC, sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * This method handles requests to the "/users/count" URL and returns the count of users.
     *
     * @return The count of users as a long value.
     */
    @GetMapping("/count")
    @Operation(
            summary = "Get user count",
            description = "Returns the total number of registered users."
    )
    public long getUserCount() {
        return userRepository.count();
    }


    /**
     * This method handles requests to the "/users/{id}" URL and returns a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The User object with the specified ID, or null if not found.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Returns the details of a user by their unique ID."
    )
    public ResponseEntity<UserDto> getUserById(@PathVariable (name = "id") Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * This method handles POST requests to create a new user.
     * It expects a JSON request body containing user details,
     * converts it to a User entity, saves it to the database,
     * and returns the created user as a UserDto.
     * @param request The request body containing user details.
     * @return The created user as a UserDto.
     */
    @PostMapping
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with the provided details."
    )
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {

        // Check if the email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error", "Email already exists"
                            )
                    );

        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER); // Set default role to CUSTOMER
        User savedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(savedUser);
        return ResponseEntity
                .created(
                        uriComponentsBuilder
                                .path("/users/{id}")
                                .buildAndExpand(savedUser.getId())
                                .toUri()
                )
                .body(userDto);
    }


    /**
     * This method handles DELETE requests to delete a user by their ID.
     * It checks if the user exists, deletes them if found, and returns a no content response.
     * @param id The ID of the user to delete.
     * @return A ResponseEntity indicating the result of the deletion operation.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user by ID",
            description = "Deletes a user by their unique ID."
    )
    public ResponseEntity<Void> deleteUser(@PathVariable (name = "id") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * This method handles PUT requests to update a user by their ID.
     * It checks if the user exists, updates their details, and returns the updated user as a UserDto.
     * @param id The ID of the user to update.
     * @param request The request body containing updated user details.
     * @return The updated user as a UserDto, or a not found response if the user does not exist.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update user by ID",
            description = "Updates the details of a user by their unique ID."
    )
    public ResponseEntity<UserDto> updateUser(
            @PathVariable (name = "id") Long id,
            @RequestBody UpdateUserRequest request
    ) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userMapper.update(request, user.get());
        User updatedUser = userRepository.save(user.get());
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }



    /**
     * This method handles POST requests to change a user's password by their ID.
     * It checks if the user exists, updates their password, and returns the updated user as a UserDto.
     * @param id The ID of the user whose password is to be changed.
     * @param request The request body containing the new password.
     * @return The updated user as a UserDto, or a not found response if the user does not exist.
     */
    @PostMapping("/{id}/change-password")
    @Operation(
            summary = "Change user password",
            description = "Changes the password of a user by their unique ID."
    )
    public ResponseEntity<Void> updateUserPassword(
            @PathVariable (name = "id") Long id,
            @Valid @RequestBody ChangeUserPasswordRequest request
    ) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if the old password matches the current password
        if (!user.get().getPassword().equals(request.getOldPassword())) {
            return ResponseEntity.status(401).build();
        }

        // Validate the new password
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if the new password is the same as the old password
        if (request.getNewPassword().equals(request.getOldPassword())) {
            return ResponseEntity.status(409).build();
        }



        user.get().setPassword(request.getNewPassword());
        userRepository.save(user.get());
        return ResponseEntity.noContent().build();
    }
}
