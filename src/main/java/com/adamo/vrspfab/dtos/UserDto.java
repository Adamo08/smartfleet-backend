package com.adamo.vrspfab.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;;
    private String lastName;
    private String email;
    private String phoneNumber;
}
