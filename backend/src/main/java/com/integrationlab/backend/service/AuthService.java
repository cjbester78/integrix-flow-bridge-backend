
package com.integrationlab.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.integrationlab.data.model.User;
import com.integrationlab.data.repository.UserRepository;
import com.integrationlab.backend.security.JwtUtil;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public Optional<User> validateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public String generateToken(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return jwtUtil.generateToken(username, user.getRole());
    }

    public String refreshToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return jwtUtil.generateToken(username, user.getRole());
    }

    public Optional<User> getUserFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return Optional.ofNullable(userRepository.findByUsername(username));
    }
}
