package com.valome.starter.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.valome.starter.model.User;
import com.valome.starter.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class JpaAuditingConfig {
    private final UserService userService;

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    private class SpringSecurityAuditorAware implements AuditorAware<Long> {
        @Override
        public Optional<Long> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
                return Optional.empty();
            }

            try {
                String username = null;
                if (authentication.getPrincipal() instanceof UserDetails) {
                    username = ((UserDetails) authentication.getPrincipal()).getUsername();
                } else if (authentication.getPrincipal() instanceof User) {
                    return Optional.of(((User) authentication.getPrincipal()).getId());
                } else if (authentication.getPrincipal() instanceof String) {
                    username = (String) authentication.getPrincipal();
                }

                if (username != null) {
                    User user = userService.findByUsername(username);
                    if (user != null && user.getId() != null) {
                        return Optional.of(user.getId());
                    }
                }
            } catch (Exception e) {
                // If we can't get the user, return empty
            }

            return Optional.empty();
        }
    }
}
