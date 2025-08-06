package com.adamo.vrspfab.testimonials;

/**
 * Custom exception to be thrown when a user attempts to submit
 * a testimonial for a vehicle for which they have already submitted one.
 */
public class DuplicateTestimonialException extends RuntimeException {
    public DuplicateTestimonialException(String message) {
        super(message);
    }
}
