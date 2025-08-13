package com.adamo.vrspfab.users;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.AuthProvider;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private AuthProvider authProvider;
    private String providerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
