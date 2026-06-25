package com.jobportal.security;

import com.jobportal.model.Role;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// JwtAuthFilter intercepts every HTTP request and validates JWT tokens.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);

            if (token != null && jwtTokenProvider.validateToken(token)){
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                Role userRole = jwtTokenProvider.getUserRoleFromToken(token);

                com.jobportal.model.User authenticatedUser = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.name());
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("userId", userId);
                request.setAttribute("userName", authenticatedUser.getName());
                request.setAttribute("userRole", userRole.name());
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer "))
            return bearerToken.substring(7);

        return null;
    }

    // Inside your com.jobportal.security.JwtAuthFilter.java file

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Bypass token checks completely for signup, login, and static auth endpoints
        return path.equals("/auth/login") || path.equals("/auth/signup");
    }
}
