package com.adamo.vrspfab.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users",
               description = "Retrieves a list of all users, with optional sorting.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public Iterable<UserDto> getAllUsers(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sortBy
    ) {
        return userService.getAllUsers(sortBy);
    }

    @Operation(summary = "Get user by ID",
               description = "Retrieves a single user by their ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
                       @ApiResponse(responseCode = "404", description = "User not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Operation(summary = "Register a new user",
               description = "Registers a new user in the system.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "User registered successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid input"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {

        var userDto = userService.registerUser(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userDto);
    }

    @Operation(summary = "Update an existing user",
               description = "Updates the details of an existing user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "User updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid input"),
                       @ApiResponse(responseCode = "404", description = "User not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Update user role",
               description = "Updates the role of a specific user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "User role updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid input"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
                       @ApiResponse(responseCode = "404", description = "User not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/role")
    public UserDto updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request
    ) {
        return userService.updateUserRole(id, request.getRole());
    }

    @Operation(summary = "Delete a user",
               description = "Deletes a user by their ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                       @ApiResponse(responseCode = "404", description = "User not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Change user password",
               description = "Changes the password for a specific user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid input or current password mismatch"),
                       @ApiResponse(responseCode = "404", description = "User not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/{id}/change-password")
    public void changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
    }
}