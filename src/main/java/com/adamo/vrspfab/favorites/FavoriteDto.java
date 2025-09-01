package com.adamo.vrspfab.favorites;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteDto {
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enriched fields for displaying favorites without extra lookups
    private String userName;
    private String userEmail;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleImageUrl;
}
