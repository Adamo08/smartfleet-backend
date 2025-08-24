package com.adamo.vrspfab.testimonials;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestimonialStatsDto {
    private long totalTestimonials;
    private long approvedTestimonials;
    private long pendingTestimonials;
    private double averageRating;
}
