package com.adamo.vrspfab.bookmarks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookmarkDto {

    @NotNull(message = "ID cannot be null")
    private Long id;
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Reservation ID cannot be null")
    private Long reservationId;

    private LocalDateTime createdAt;

    @NotBlank (message = "Note cannot be blank")
    private String note;
}