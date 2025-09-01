package com.adamo.vrspfab.favorites;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Tag(name = "Favorite Management", description = "APIs for managing user's favorite vehicles")
@AllArgsConstructor
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    @Operation(summary = "Add a vehicle to favorites",
               description = "Adds a vehicle to the current user's list of favorite vehicles.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Favorite created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid favorite data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteDto createFavorite(@Valid @RequestBody FavoriteDto favoriteDto) {
        logger.info("Received request to create favorite: {}", favoriteDto);
        return favoriteService.createFavorite(favoriteDto);
    }

    @Operation(summary = "Get favorite by ID",
               description = "Retrieves a single favorite entry by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved favorite"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this favorite"),
                       @ApiResponse(responseCode = "404", description = "Favorite not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<FavoriteDto> getFavorite(@PathVariable Long id) {
        logger.info("Received request to get favorite with ID: {}", id);
        return ResponseEntity.ok(favoriteService.getFavoriteById(id));
    }

    @Operation(summary = "Delete a favorite entry",
               description = "Deletes a favorite entry by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Favorite deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this favorite"),
                       @ApiResponse(responseCode = "404", description = "Favorite not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFavorite(@PathVariable Long id) {
        logger.info("Received request to delete favorite with ID: {}", id);
        favoriteService.deleteFavorite(id);
    }

    @Operation(summary = "Delete all favorite entries for current user",
               description = "Deletes all favorite entries for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "All favorites deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/my/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllMyFavorites() {
        logger.info("Received request to delete all favorites for current user");
        favoriteService.deleteAllMyFavorites();
    }

    @Operation(summary = "Get all favorite entries (Admin only)",
               description = "Retrieves a paginated list of all favorite entries in the system, with optional filtering by user ID. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved favorites"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<FavoriteDto>> getAllFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get all favorites with filters: page={}, size={}, userId={}, sortBy={}, sortDirection={}",
                page, size, userId, sortBy, sortDirection);
        Page<FavoriteDto> favoritesPage = favoriteService.getAllFavorites(page, size, userId, sortBy, sortDirection);
        return ResponseEntity.ok(favoritesPage);
    }

    @Operation(summary = "Get current user's favorite entries",
               description = "Retrieves a paginated list of favorite entries for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user's favorites"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/my")
    public ResponseEntity<Page<FavoriteDto>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get favorites for current authenticated user: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(favoriteService.getMyFavorites(page, size, sortBy, sortDirection));
    }
}
