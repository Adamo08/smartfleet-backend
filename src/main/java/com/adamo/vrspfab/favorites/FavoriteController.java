package com.adamo.vrspfab.favorites;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final FavoriteMapper favoriteMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteDto createFavorite(@RequestBody FavoriteDto favoriteDto) {
        return favoriteService.createFavorite(favoriteDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FavoriteDto> getFavorite(@PathVariable Long id) {
        return ResponseEntity.ok(favoriteService.getFavoriteById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFavorite(@PathVariable Long id) {
        favoriteService.deleteFavorite(id);
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDto>> getAllFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(favoriteService.getAllFavorites(page, size, userId));
    }
}