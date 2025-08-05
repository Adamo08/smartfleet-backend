package com.adamo.vrspfab.favorites;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteDto {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private LocalDateTime createdAt;
}
