package com.adamo.vrspfab.testimonials;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/testimonials")
public class TestimonialController {

    private final TestimonialService testimonialService;
    private final TestimonialMapper testimonialMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestimonialDto createTestimonial(@RequestBody TestimonialDto testimonialDto) {
        return testimonialService.createTestimonial(testimonialDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestimonialDto> getTestimonial(@PathVariable Long id) {
        return ResponseEntity.ok(testimonialService.getTestimonialById(id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TestimonialDto> approveTestimonial(@PathVariable Long id) {
        return ResponseEntity.ok(testimonialService.approveTestimonial(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTestimonial(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
    }

    @GetMapping
    public ResponseEntity<List<TestimonialDto>> getAllTestimonials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isApproved) {
        return ResponseEntity.ok(testimonialService.getAllTestimonials(page, size, userId, isApproved));
    }
}