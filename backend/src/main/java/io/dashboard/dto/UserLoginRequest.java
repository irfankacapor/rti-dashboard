package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
} 