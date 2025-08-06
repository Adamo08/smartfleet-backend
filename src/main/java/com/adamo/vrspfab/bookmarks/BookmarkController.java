package com.adamo.vrspfab.bookmarks;

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
@RequestMapping("/bookmarks")
public class BookmarkController {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkController.class);

    private final BookmarkService bookmarkService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkDto createBookmark(@Valid @RequestBody BookmarkDto bookmarkDto) {
        logger.info("Received request to create bookmark: {}", bookmarkDto);
        return bookmarkService.createBookmark(bookmarkDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkDto> getBookmark(@PathVariable Long id) {
        logger.info("Received request to get bookmark with ID: {}", id);
        return ResponseEntity.ok(bookmarkService.getBookmarkById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookmark(@PathVariable Long id) {
        logger.info("Received request to delete bookmark with ID: {}", id);
        bookmarkService.deleteBookmark(id);
    }

    @GetMapping
    public ResponseEntity<Page<BookmarkDto>> getAllBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get all bookmarks with filters: page={}, size={}, userId={}, sortBy={}, sortDirection={}",
                page, size, userId, sortBy, sortDirection);
        Page<BookmarkDto> bookmarksPage = bookmarkService.getAllBookmarks(page, size, userId, sortBy, sortDirection);
        return ResponseEntity.ok(bookmarksPage);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BookmarkDto>> getMyBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get bookmarks for current authenticated user: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(bookmarkService.getMyBookmarks(page, size, sortBy, sortDirection));
    }
}
