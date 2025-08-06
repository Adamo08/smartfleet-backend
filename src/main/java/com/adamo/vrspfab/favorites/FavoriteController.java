package com.adamo.vrspfab.favorites;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@AllArgsConstructor
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteDto createFavorite(@Valid @RequestBody FavoriteDto favoriteDto) {
        logger.info("Received request to create favorite: {}", favoriteDto);
        return favoriteService.createFavorite(favoriteDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FavoriteDto> getFavorite(@PathVariable Long id) {
        logger.info("Received request to get favorite with ID: {}", id);
        return ResponseEntity.ok(favoriteService.getFavoriteById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFavorite(@PathVariable Long id) {
        logger.info("Received request to delete favorite with ID: {}", id);
        favoriteService.deleteFavorite(id);
    }

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
