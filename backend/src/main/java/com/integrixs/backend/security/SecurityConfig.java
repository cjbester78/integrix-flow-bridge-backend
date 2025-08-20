package com.integrixs.backend.security;

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

import com.integrixs.data.repository.UserRepository;
import com.integrixs.backend.service.UserService;

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
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        filter.setUserRepository(userRepository);
        return filter;
    }

    @Bean
    public UserContextFilter userContextFilter(UserRepository userRepository) {
        return new UserContextFilter(userRepository);
    }
    
    @Bean
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.setEnabled(false); // Prevent double registration by Spring
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                         UserContextFilter userContextFilter,
                                         CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                         CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        return http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // CSRF disabled for stateless JWT authentication
                .csrf(csrf -> csrf.disable())
                
                // Security headers
                .headers(headers -> headers
                    .frameOptions(frame -> frame.deny())
                    .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .contentTypeOptions(content -> content.disable())
                    .referrerPolicy(referrer -> 
                        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicy(permissions -> 
                        permissions.policy("camera=(), microphone=(), geolocation=()"))
                )
                
                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        // CRITICAL: Allow all SPA routes without authentication
                        .requestMatchers(
                                "/login/**",
                                "/login",
                                "/dashboard/**",
                                "/dashboard",
                                "/settings/**",
                                "/settings",
                                "/admin/**",
                                "/admin"
                        ).permitAll()
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
                                "/favicon.ico",
                                "/robots.txt",
                                "/static/**",
                                "/assets/**",
                                "/*.js",
                                "/*.css",
                                "/*.svg",
                                "/*.png",
                                "/*.jpg",
                                "/*.woff2",
                                "/*.ttf",
                                "/ws/**",
                                "/flow-execution",
                                "/ws/messages",
                                "/ws/flow-execution",
                                "/ws/flow-execution-native",
                                "/echo",
                                "/test-ws",
                                "/wstest",
                                "/direct-ws",
                                "/minimal-echo",
                                "/basic"
                        ).permitAll()
                        .requestMatchers("/soap/**").permitAll() // Allow SOAP endpoints without authentication
                        .requestMatchers("/api/test-deployed-flows").permitAll() // Test endpoint
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/websocket-test/**").permitAll()
                        .requestMatchers("/api/system-settings/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRATOR", "DEVELOPER")
                        .requestMatchers("/api/flows/execute/**").hasAnyRole("ADMINISTRATOR", "DEVELOPER", "INTEGRATOR")
                        .requestMatchers("/api/flows/**").hasAnyRole("ADMINISTRATOR", "DEVELOPER", "VIEWER")
                        .requestMatchers("/api/**").hasAnyRole("ADMINISTRATOR", "DEVELOPER", "INTEGRATOR", "VIEWER")
                        .anyRequest().permitAll() // Change to permitAll for SPA routes
                )
                
                // Session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                // UserContext filter (after JWT authentication)
                .addFilterAfter(userContextFilter, JwtAuthFilter.class)
                
                // Exception handling
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
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
