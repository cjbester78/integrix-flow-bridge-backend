package com.integrixs.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.integrixs.data.repository.UserRepository;
import com.integrixs.data.model.User;


public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private UserRepository userRepository;

    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                
                System.out.println("JWT Auth - Username: " + username + ", Role: " + role);
                
                if (role == null) {
                    // For old tokens without role, invalidate them
                    System.out.println("JWT Auth - Token missing role information, rejecting");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\":\"Token missing required role information. Please login again.\"}");
                    return;
                }
                
                // Load the User entity from the database
                User user = null;
                if (userRepository != null) {
                    user = userRepository.findByUsername(username);
                }
                
                // Create authorities list with ROLE_ prefix for Spring Security
                // Convert role to uppercase to match Spring Security expectations
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );
                
                System.out.println("JWT Auth - Authorities: " + authorities);
                
                // Set either the User entity or username as principal
                var auth = new UsernamePasswordAuthenticationToken(
                        user != null ? user : username,
                        null,
                        authorities
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                System.out.println("JWT Auth - Authentication set for: " + request.getRequestURI() + " with principal: " + (user != null ? "User entity" : "username"));
            } else {
                System.out.println("JWT Auth - Invalid token for: " + request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip JWT filter for WebSocket upgrade requests
        String upgradeHeader = request.getHeader("Upgrade");
        if ("websocket".equalsIgnoreCase(upgradeHeader)) {
            return true;
        }
        
        return path.startsWith("/auth") || 
               path.equals("/health") ||
               path.startsWith("/ws/") ||
               path.equals("/flow-execution") ||
               path.equals("/echo") ||
               path.equals("/test-ws") ||
               path.equals("/wstest") ||
               path.equals("/direct-ws") ||
               path.equals("/minimal-echo") ||
               path.equals("/basic");
    }
}