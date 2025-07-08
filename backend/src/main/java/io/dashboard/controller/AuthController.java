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
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        UserResponse user = userService.login(request);
        String token = jwtUtil.generateToken(userService.getUserEntity(user.getUsername()));
        response.addHeader("Set-Cookie", "token=" + token + "; HttpOnly; Path=/; Max-Age=86400");
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "token=; HttpOnly; Path=/; Max-Age=0");
        return ResponseEntity.ok().build();
    }
} 