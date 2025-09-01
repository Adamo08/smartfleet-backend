package com.adamo.vrspfab.testimonials;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestimonialDto {

    private Long id;

    @NotNull (message = "User ID cannot be null")
    private Long userId;
    private Long vehicleId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean approved;


    private String adminReplyContent;

    // Enriched fields for displaying testimonials without extra lookups
    private String userName;
    private String userEmail;
    private String vehicleBrand;
    private String vehicleModel;
}
