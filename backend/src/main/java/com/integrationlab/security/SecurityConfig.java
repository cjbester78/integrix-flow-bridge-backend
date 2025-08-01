package com.integrationlab.security;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Enhanced security configuration with comprehensive security features.
 * 
 * <p>Provides JWT authentication, CORS configuration, security headers,
 * and method-level security.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil);
    }

    @Bean
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.setEnabled(false); // Prevent double registration by Spring
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // CSRF disabled for stateless JWT authentication
                .csrf(csrf -> csrf.disable())
                
                // Security headers
                .headers(headers -> headers
                    .frameOptions(frame -> frame.deny())
                    .xssProtection(xss -> xss.headerValue("1; mode=block"))
                    .contentTypeOptions(content -> content.disable())
                    .referrerPolicy(referrer -> 
                        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicy(permissions -> 
                        permissions.policy("camera=(), microphone=(), geolocation=()"))
                    .contentSecurityPolicy(csp -> 
                        csp.policyDirectives("default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data: https:; " +
                            "font-src 'self' data:; " +
                            "connect-src 'self' ws: wss:; " +
                            "frame-ancestors 'none';"))
                )
                
                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/api/auth/refresh",
                                "/health",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/",
                                "/index.html",
                                "/login",
                                "/favicon.ico",
                                "/robots.txt",
                                "/static/**",
                                "/assets/**",
                                "/**/*.js",
                                "/**/*.css",
                                "/**/*.svg",
                                "/**/*.png",
                                "/**/*.jpg",
                                "/**/*.woff2",
                                "/**/*.ttf"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/flows/**").hasAnyRole("ADMINISTRATOR", "INTEGRATOR")
                        .requestMatchers("/api/**").hasAnyRole("ADMINISTRATOR", "INTEGRATOR", "VIEWER")
                        .anyRequest().authenticated()
                )
                
                // Session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                // Exception handling
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + 
                            authException.getMessage() + "\"}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"" + 
                            accessDeniedException.getMessage() + "\"}");
                    })
                )
                
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength
    }
    
    /**
     * CORS configuration for secure cross-origin requests.
     * 
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
