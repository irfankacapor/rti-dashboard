package io.dashboard.controller;

import io.dashboard.dto.UserRegisterRequest;
import io.dashboard.dto.UserLoginRequest;
import io.dashboard.dto.UserResponse;
import io.dashboard.service.UserService;
import io.dashboard.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;
import io.dashboard.dto.UserCreateByAdminRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        // Force all public registrations to USER role
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    @PermitAll
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        UserResponse user = userService.login(request);
        String token = jwtUtil.generateToken(userService.getUserEntity(user.getUsername()));
        response.addHeader("Set-Cookie", "token=" + token + "; HttpOnly; Path=/; Max-Age=14400; SameSite=None; Secure"); // 4 hours
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @PermitAll
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "token=; HttpOnly; Path=/; Max-Age=0; SameSite=None; Secure");
        return ResponseEntity.ok().build();
    }

    // Admin-only endpoint to create users with specific roles
    @PostMapping("/create-user")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateByAdminRequest request) {
        UserResponse user = userService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
} 