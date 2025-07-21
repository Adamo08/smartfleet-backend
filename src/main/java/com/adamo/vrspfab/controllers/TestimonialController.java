package com.adamo.vrspfab.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testimonials")
public class TestimonialController {

    @GetMapping
    public String getTestimonials() {
        return "List of testimonials";
    }

    @PostMapping
    public String createTestimonial() {
        return "Testimonial created";
    }
}
