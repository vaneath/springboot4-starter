package com.valome.starter.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.jpa.user.UserJpaRepository;
import com.valome.starter.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository jpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public User register(RegisterRequest request) {
        // Check if username already exists
        if (jpaRepository.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save and return user
        return jpaRepository.save(user);
    }
}
