package io.dashboard.config;

import io.dashboard.util.JwtUtil;
import io.dashboard.model.UserRole;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthFilter implements Filter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        // Allow public endpoints
        if (path.startsWith("/api/auth") || path.startsWith("/api/public") || path.equals("/api/health")) {
            chain.doFilter(request, response);
            return;
        }

        String token = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Unauthorized: Invalid or missing token");
            return;
        }

        // Optionally, set user info as request attribute for downstream use
        req.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        req.setAttribute("role", jwtUtil.getRoleFromToken(token));

        chain.doFilter(request, response);
    }
} 