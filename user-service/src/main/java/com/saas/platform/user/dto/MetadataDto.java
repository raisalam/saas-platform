package com.saas.platform.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class MetadataDto {
    @JsonProperty("app-version")
    private String appVersion;

    private Object securityInfo; // usually {} → can be Map or Object

    private String hash;
}