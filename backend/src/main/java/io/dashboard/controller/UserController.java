package io.dashboard.controller;

import io.dashboard.dto.UserResponse;
import io.dashboard.model.User;
import io.dashboard.service.UserService;
import io.dashboard.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            throw new ResourceNotFoundException("User not found in request context");
        }
        User user = userService.getUserEntity(username);
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole());
        return ResponseEntity.ok(resp);
    }
} 