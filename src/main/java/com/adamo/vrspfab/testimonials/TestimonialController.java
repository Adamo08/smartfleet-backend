package com.adamo.vrspfab.testimonials;

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



@Tag(name = "Testimonial Management", description = "APIs for managing user testimonials")
@AllArgsConstructor
@RestController
@RequestMapping("/testimonials")
public class TestimonialController {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);

    private final TestimonialService testimonialService;

    @Operation(summary = "Create a new testimonial",
               description = "Creates a new testimonial. Testimonials typically require approval before becoming public.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Testimonial created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid testimonial data"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestimonialDto createTestimonial(@Valid @RequestBody TestimonialDto testimonialDto) {
        logger.info("Received request to create testimonial: {}", testimonialDto);
        return testimonialService.createTestimonial(testimonialDto);
    }

    @Operation(summary = "Get testimonial by ID",
               description = "Retrieves a single testimonial by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved testimonial"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<TestimonialDto> getTestimonial(@PathVariable Long id) {
        logger.info("Received request to get testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.getTestimonialById(id));
    }

    @Operation(summary = "Approve a testimonial",
               description = "Approves a testimonial, making it visible to the public. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Testimonial approved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}/approve")
    public ResponseEntity<TestimonialDto> approveTestimonial(@PathVariable Long id) {
        logger.info("Received request to approve testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.approveTestimonial(id));
    }

    @Operation(summary = "Update a testimonial",
               description = "Updates the details of an existing testimonial.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Testimonial updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid testimonial data"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}")
    public ResponseEntity<TestimonialDto> updateTestimonial(@PathVariable Long id, @Valid @RequestBody TestimonialDto testimonialDto) {
        logger.info("Received request to update testimonial with ID: {}", id);
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, testimonialDto));
    }

    @Operation(summary = "Delete a testimonial",
               description = "Deletes a testimonial by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Testimonial deleted successfully"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTestimonial(@PathVariable Long id) {
        logger.info("Received request to delete testimonial with ID: {}", id);
        testimonialService.deleteTestimonial(id);
    }

    @Operation(summary = "Get all testimonials with pagination and filters",
               description = "Retrieves a paginated list of all testimonials, with optional filtering by user, vehicle, and approval status.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved testimonials"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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

    @Operation(summary = "Get public approved testimonials",
               description = "Retrieves a paginated list of publicly approved testimonials, with optional filtering by vehicle.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved public testimonials"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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


    @Operation(summary = "Get testimonials for authenticated user",
               description = "Retrieves a paginated list of testimonials submitted by the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user's testimonials"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
