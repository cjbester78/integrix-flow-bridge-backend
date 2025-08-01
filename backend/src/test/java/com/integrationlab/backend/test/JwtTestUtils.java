package com.integrationlab.backend.test;

import com.integrationlab.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * JWT utilities for testing.
 * 
 * <p>Provides methods to create test JWT tokens and authentication objects.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class JwtTestUtils {
    
    private static final String TEST_SECRET = "test-secret-key-for-testing-only-must-be-256-bits";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    private static final long EXPIRATION = 3600000; // 1 hour
    
    /**
     * Create a test JWT token.
     */
    public static String createToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("userId", UUID.randomUUID().toString());
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Create a test JWT token for admin user.
     */
    public static String createAdminToken() {
        return createToken("admin", "administrator");
    }
    
    /**
     * Create a test JWT token for integrator user.
     */
    public static String createIntegratorToken() {
        return createToken("integrator", "integrator");
    }
    
    /**
     * Create a test JWT token for viewer user.
     */
    public static String createViewerToken() {
        return createToken("viewer", "viewer");
    }
    
    /**
     * Create an expired JWT token.
     */
    public static String createExpiredToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 1000); // Already expired
        
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Create an Authentication object for testing.
     */
    public static Authentication createAuthentication(String username, String role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
    
    /**
     * Create Authorization header value.
     */
    public static String createAuthorizationHeader(String token) {
        return "Bearer " + token;
    }
    
    /**
     * Mock JwtUtil for testing.
     */
    public static JwtUtil createMockJwtUtil() {
        return new JwtUtil() {
            @Override
            public String extractUsername(String token) {
                return Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
            }
            
            @Override
            public Date extractExpiration(String token) {
                return Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();
            }
            
            @Override
            public Boolean isTokenExpired(String token) {
                return extractExpiration(token).before(new Date());
            }
            
            @Override
            public Boolean validateToken(String token, String username) {
                final String extractedUsername = extractUsername(token);
                return (extractedUsername.equals(username) && !isTokenExpired(token));
            }
        };
    }
}