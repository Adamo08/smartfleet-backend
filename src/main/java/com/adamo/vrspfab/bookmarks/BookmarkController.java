package com.adamo.vrspfab.bookmarks;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {


    private final BookmarkService bookmarkService;
    private final BookmarkMapper bookmarkMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkDto createBookmark(@Valid @RequestBody BookmarkDto bookmarkDTO) {
        return bookmarkService.createBookmark(bookmarkDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkDto> getBookmark(@PathVariable Long id) {
        return ResponseEntity.ok(bookmarkService.getBookmarkById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);
    }

    @GetMapping
    public ResponseEntity<List<BookmarkDto>> getAllBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(bookmarkService.getAllBookmarks(page, size, userId));
    }
}