package com.saas.platform.catalog.mapper;

import com.saas.platform.catalog.dto.KeyItemResponse;
import com.saas.platform.catalog.entity.SubscriptionKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KeyMapper {

    @Mapping(target = "gameId", source = "gameId")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    KeyItemResponse toDto(
            SubscriptionKey entity,
            Long gameId,
            Integer durationMinutes
    );
}
