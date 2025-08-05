package com.adamo.vrspfab.testimonials;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestimonialDto {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
    private boolean isApproved;
}