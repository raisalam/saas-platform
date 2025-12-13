package com.saas.platform.catalog.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class KeyGenerationRequest {

    private Map<String, Map<String, Integer>> cart = new HashMap<>();
    private DeviceInfoDto deviceInfo;
    private MetadataDto metadata;
    private String androidId;


    @JsonAnySetter
    public void addCartEntry(String key, Map<String, Integer> value) {
        if (key.equals("deviceInfo") || key.equals("metadata") || key.equals("androidId")) {
            return;
        }
        cart.put(key, value);
    }
}

