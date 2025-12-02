package com.valome.starter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    protected Long id;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime deletedAt;

    @Transient
    @Default
    private boolean isDeleted = false;

    @Default
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        // Timestamps are handled by @CreatedDate and @LastModifiedDate via
        // AuditingEntityListener
        // User IDs are handled by @CreatedBy and @LastModifiedBy via AuditorAware
        // This is a fallback to ensure timestamps are set if auditing fails
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        // Fallback: if createdBy is not set by AuditorAware, try to get it from
        // SecurityContext
        if (createdBy == null) {
            createdBy = getCurrentUserIdFromContext();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Timestamp is handled by @LastModifiedDate via AuditingEntityListener
        // User ID is handled by @LastModifiedBy via AuditorAware
        // This is a fallback to ensure timestamp is set if auditing fails
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        // Fallback: if updatedBy is not set by AuditorAware, try to get it from
        // SecurityContext
        if (updatedBy == null) {
            updatedBy = getCurrentUserIdFromContext();
        }
    }

    /**
     * Fallback method to get current user ID from SecurityContext.
     * This is used if AuditorAware doesn't set the audit fields.
     * Primary mechanism is via AuditorAware configured in JpaAuditingConfig.
     */
    private Long getCurrentUserIdFromContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
                return null;
            }

            // If principal is already a User object, get ID directly
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return user.getId();
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID from SecurityContext for auditing", e);
        }
        return null;
    }
}