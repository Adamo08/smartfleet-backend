package com.adamo.vrspfab.users;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must be less than 255 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must be less than 255 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Email(message = "Email must be valid")
    private String email;
    private String phoneNumber;
}
