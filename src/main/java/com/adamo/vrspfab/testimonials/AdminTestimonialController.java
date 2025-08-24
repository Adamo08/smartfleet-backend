package com.adamo.vrspfab.testimonials;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/testimonials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Testimonials", description = "APIs for administrators to manage testimonials")
public class AdminTestimonialController {
    
    private final TestimonialService testimonialService;
    
    @GetMapping
    @Operation(summary = "Get all testimonials with pagination and filters",
               description = "Retrieves a paginated list of all testimonials with admin-level access. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved testimonials"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Page<TestimonialDto>> getAllTestimonials(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Boolean approved,
            Pageable pageable) {
        log.info("Admin requested testimonials with filters: userId={}, vehicleId={}, approved={}", userId, vehicleId, approved);
        Page<TestimonialDto> testimonials = testimonialService.getAllTestimonials(userId, vehicleId, approved, pageable);
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending testimonials",
               description = "Retrieves a list of testimonials awaiting approval. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved pending testimonials"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<List<TestimonialDto>> getPendingTestimonials() {
        log.info("Admin requested pending testimonials");
        List<TestimonialDto> testimonials = testimonialService.getPendingTestimonials();
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get testimonial by ID",
               description = "Retrieves a single testimonial by its ID with admin-level access. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved testimonial"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialDto> getTestimonialById(@PathVariable Long id) {
        log.info("Admin requested testimonial with ID: {}", id);
        TestimonialDto testimonial = testimonialService.getTestimonialById(id);
        return ResponseEntity.ok(testimonial);
    }
    
    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a testimonial",
               description = "Approves a testimonial, making it visible to the public. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Testimonial approved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialDto> approveTestimonial(@PathVariable Long id) {
        log.info("Admin approving testimonial with ID: {}", id);
        TestimonialDto testimonial = testimonialService.approveTestimonial(id);
        return ResponseEntity.ok(testimonial);
    }
    
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a testimonial",
               description = "Rejects a testimonial with optional reason. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Testimonial rejected successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialDto> rejectTestimonial(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("Admin rejecting testimonial with ID: {}, reason: {}", id, reason);
        TestimonialDto testimonial = testimonialService.rejectTestimonial(id, reason);
        return ResponseEntity.ok(testimonial);
    }
    
    @PutMapping("/{id}/admin-reply")
    @Operation(summary = "Add admin reply to testimonial",
               description = "Adds or updates an admin reply to a testimonial. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Admin reply added successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid reply data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialDto> addAdminReply(
            @PathVariable Long id,
            @Valid @RequestBody TestimonialDto testimonialDto) {
        log.info("Admin adding reply to testimonial with ID: {}", id);
        TestimonialDto testimonial = testimonialService.addAdminReply(id, testimonialDto.getAdminReplyContent());
        return ResponseEntity.ok(testimonial);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update testimonial",
               description = "Updates an existing testimonial with admin-level access. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Testimonial updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid testimonial data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialDto> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody TestimonialDto testimonialDto) {
        log.info("Admin updating testimonial with ID: {}", id);
        TestimonialDto testimonial = testimonialService.updateTestimonial(id, testimonialDto);
        return ResponseEntity.ok(testimonial);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete testimonial",
               description = "Deletes a testimonial by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Testimonial deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Testimonial not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
        log.info("Admin deleting testimonial with ID: {}", id);
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get testimonial statistics",
               description = "Retrieves statistics about testimonials for admin dashboard. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<TestimonialStatsDto> getTestimonialStats() {
        log.info("Admin requested testimonial statistics");
        TestimonialStatsDto stats = testimonialService.getTestimonialStats();
        return ResponseEntity.ok(stats);
    }
}
