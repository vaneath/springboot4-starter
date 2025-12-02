package com.valome.starter.dto.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BaseRequest {
    @JsonProperty("isActive")
    private Boolean isActive;
}
