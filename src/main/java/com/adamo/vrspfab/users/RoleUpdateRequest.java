package com.adamo.vrspfab.users;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull
    private Role role;
}


