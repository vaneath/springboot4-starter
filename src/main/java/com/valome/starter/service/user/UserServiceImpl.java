package com.valome.starter.service.user;

import org.springframework.stereotype.Service;

import com.valome.starter.jpa.user.UserJpaRepository;
import com.valome.starter.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository jpaRepository;

    @Override
    public User findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }
}
