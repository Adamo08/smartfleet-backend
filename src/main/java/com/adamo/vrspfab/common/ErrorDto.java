package com.adamo.vrspfab.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
