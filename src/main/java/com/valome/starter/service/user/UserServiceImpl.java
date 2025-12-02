package com.valome.starter.service.user;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valome.starter.builder.SortBuilder;
import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.jpa.user.UserJpaRepository;
import com.valome.starter.model.User;
import com.valome.starter.service.search.GenericSpecification;
import com.valome.starter.exception.ResourceNotFoundException;

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

    @Override
    public User getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        } else {
            throw new IllegalArgumentException("Invalid authentication principal");
        }

        User user = findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(PaginationRequest request) {
        // Handle null request - create default initialized request
        if (request == null) {
            request = PaginationRequest.createDefault();
        } else {
            request.ensureInitialized();
        }

        // Build specification for filtering and searching
        GenericSpecification<User> spec = new GenericSpecification<>(request, User.PAGINATION_FIELDS);

        // Build sort
        Sort sort = SortBuilder.build(request.getSorts(), User.PAGINATION_FIELDS);

        // Build pageable
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<User> page = jpaRepository.findAll(spec, pageable);

        page.getContent().forEach(user -> {
            // Initialize userRoles collection (triggers batch loading)
            Hibernate.initialize(user.getUserRoles());
            // Initialize nested Role entities to prevent LazyInitializationException
            if (user.getUserRoles() != null) {
                user.getUserRoles().forEach(userRole -> {
                    Hibernate.initialize(userRole.getRole());
                });
            }
        });

        return page;
    }
}
