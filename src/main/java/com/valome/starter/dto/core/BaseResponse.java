package com.valome.starter.dto.core;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseResponse {
    private String id;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    /**
     * Active status of the entity.
     * Uses @JsonProperty to ensure correct JSON serialization/deserialization
     * when field name starts with "is" prefix.
     */
    @JsonProperty("isActive")
    private Boolean isActive;
}
