package com.adamo.vrspfab.bookmarks;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookmarkDto {
    private Long id;
    private Long userId;
    private Long reservationId;
    private LocalDateTime createdAt;
    private String note;
}