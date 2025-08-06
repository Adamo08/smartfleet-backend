package com.adamo.vrspfab.testimonials;

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
@RequestMapping("/testimonials")
public class TestimonialController {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);

    private final TestimonialService testimonialService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestimonialDto createTestimonial(@Valid @RequestBody TestimonialDto testimonialDto) {
        logger.info("Received request to create testimonial: {}", testimonialDto);
        return testimonialService.createTestimonial(testimonialDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestimonialDto> getTestimonial(@PathVariable Long id) {
        logger.info("Received request to get testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.getTestimonialById(id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TestimonialDto> approveTestimonial(@PathVariable Long id) {
        logger.info("Received request to approve testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.approveTestimonial(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestimonialDto> updateTestimonial(@PathVariable Long id, @Valid @RequestBody TestimonialDto testimonialDto) {
        logger.info("Received request to update testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, testimonialDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTestimonial(@PathVariable Long id) {
        logger.info("Received request to delete testimonial with ID: {}", id);
        testimonialService.deleteTestimonial(id);
    }

    @GetMapping
    public ResponseEntity<Page<TestimonialDto>> getAllTestimonials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get all testimonials with filters: page={}, size={}, userId={}, vehicleId={}, isApproved={}, sortBy={}, sortDirection={}",
                page, size, userId, vehicleId, isApproved, sortBy, sortDirection);
        Page<TestimonialDto> testimonialsPage = testimonialService.getAllTestimonials(page, size, userId, vehicleId, isApproved, sortBy, sortDirection);
        return ResponseEntity.ok(testimonialsPage);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<TestimonialDto>> getPublicApprovedTestimonials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get public approved testimonials with filters: page={}, size={}, vehicleId={}, sortBy={}, sortDirection={}",
                page, size, vehicleId, sortBy, sortDirection);
        Page<TestimonialDto> testimonialsPage = testimonialService.getApprovedTestimonials(page, size, vehicleId, sortBy, sortDirection);
        return ResponseEntity.ok(testimonialsPage);
    }


    @GetMapping("/my")
    public ResponseEntity<Page<TestimonialDto>> getMyTestimonials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get testimonials for authenticated user: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        Page<TestimonialDto> testimonialsPage = testimonialService.getMyTestimonials(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(testimonialsPage);
    }
}
