package com.saas.platform.user.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddDeleteSellerDto {
    private Long sellerId;
    private String email;
    private Double amount;
    private DeviceInfoDto deviceInfo;
    private MetadataDto metadata;
}
