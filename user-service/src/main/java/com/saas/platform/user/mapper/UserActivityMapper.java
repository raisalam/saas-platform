// Java
package com.saas.platform.user.mapper;

import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.UserActivityDto;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.User;
import com.saas.platform.user.entity.UserActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserActivityMapper {


    UserActivityDto toResponse(UserActivity user);
}