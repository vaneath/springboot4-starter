package com.valome.starter.dto.core;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BaseResponse {
    private String id;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    private boolean active;
}
