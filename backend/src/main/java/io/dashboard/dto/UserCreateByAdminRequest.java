package io.dashboard.dto;

import io.dashboard.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateByAdminRequest {
    @NotBlank
    private String username;
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private UserRole role; // ADMIN, MANAGER, or USER
} 