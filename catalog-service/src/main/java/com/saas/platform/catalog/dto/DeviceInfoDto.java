package com.saas.platform.catalog.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeviceInfoDto {

    private String brand;
    private String device;
    private String model;
    private String manufacturer;
    private String id;
    private String androidId;
    private String version;
    private int sdkInt;
}
