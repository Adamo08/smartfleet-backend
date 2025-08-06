package com.adamo.vrspfab.bookmarks;

/**
 * Custom exception to be thrown when a user attempts to add a bookmark
 * for a vehicle that they have already bookmarked.
 */
public class DuplicateBookmarkException extends RuntimeException {
    public DuplicateBookmarkException(String message) {
        super(message);
    }
}
