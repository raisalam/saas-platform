package com.saas.platform.user.mapper;

import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // RegisterRequest → User (Entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)   // set manually after hashing
    @Mapping(target = "balance", constant = "0.0")
    @Mapping(target = "total", constant = "0.0")
    @Mapping(target = "fullName")
    @Mapping(target = "email")


    User toEntity(RegisterRequest dto);

    // User → UserResponse (for API responses)
    UserResponse toResponse(User user);
}
