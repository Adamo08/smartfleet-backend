package com.adamo.vrspfab.users;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;;
    private String lastName;
    private String email;
    private String phoneNumber;
}
