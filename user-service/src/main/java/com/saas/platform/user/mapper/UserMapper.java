// Java
package com.saas.platform.user.mapper;

import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapperHelper.class}
)
public interface UserMapper {

    // ❌ REMOVE THIS LINE: It conflicts with componentModel = "spring"
    // UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // RegisterRequest → User (Entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "balance", constant = "0.0")
    @Mapping(target = "total", constant = "0.0")
    @Mapping(target = "fullName")
    @Mapping(target = "email")
    User toEntity(RegisterRequest dto);

    // User → UserResponse (for API responses)
    @Mapping(target = "avatarUrl", source = "user")
    UserResponse toResponse(User user);
}