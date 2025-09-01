package com.adamo.vrspfab.favorites;


/**
 * Custom exception to be thrown when a user attempts to add a favorite
 * for a vehicle that they have already favorited.
 */
public class DuplicateFavoriteException extends RuntimeException {
    public DuplicateFavoriteException(String message) {
        super(message);
    }
}
