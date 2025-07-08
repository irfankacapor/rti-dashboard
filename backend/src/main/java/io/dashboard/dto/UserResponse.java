package io.dashboard.dto;

import io.dashboard.model.UserRole;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
} 