package com.adamo.vrspfab.bookmarks;

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


@Tag(name = "Bookmark Management", description = "APIs for managing user's bookmarks")
@AllArgsConstructor
@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkController.class);

    private final BookmarkService bookmarkService;

    @Operation(summary = "Create a new bookmark",
               description = "Adds a new bookmark for the current user.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Bookmark created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid bookmark data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "409", description = "Duplicate bookmark"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkDto createBookmark(@Valid @RequestBody BookmarkDto bookmarkDto) {
        logger.info("Received request to create bookmark: {}", bookmarkDto);
        return bookmarkService.createBookmark(bookmarkDto);
    }

    @Operation(summary = "Get bookmark by ID",
               description = "Retrieves a single bookmark by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved bookmark"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this bookmark"),
                       @ApiResponse(responseCode = "404", description = "Bookmark not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<BookmarkDto> getBookmark(@PathVariable Long id) {
        logger.info("Received request to get bookmark with ID: {}", id);
        return ResponseEntity.ok(bookmarkService.getBookmarkById(id));
    }

    @Operation(summary = "Delete a bookmark",
               description = "Deletes a bookmark by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Bookmark deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this bookmark"),
                       @ApiResponse(responseCode = "404", description = "Bookmark not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookmark(@PathVariable Long id) {
        logger.info("Received request to delete bookmark with ID: {}", id);
        bookmarkService.deleteBookmark(id);
    }

    @Operation(summary = "Get all bookmarks (Admin only)",
               description = "Retrieves a paginated list of all bookmarks in the system, with optional filtering by user ID. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved bookmarks"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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

    @Operation(summary = "Get current user's bookmarks",
               description = "Retrieves a paginated list of bookmarks for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user's bookmarks"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
