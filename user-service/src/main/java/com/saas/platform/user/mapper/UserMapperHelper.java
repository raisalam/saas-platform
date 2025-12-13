// Java
package com.saas.platform.user.mapper;

import com.saas.platform.user.entity.User;
import com.saas.platform.user.entity.UserAttribute;
import org.springframework.stereotype.Component;

/**
 * Helper class used by MapStruct to resolve complex mappings (like avatarUrl from attributes).
 */
@Component
public class UserMapperHelper {

    // MapStruct will use this method to map User (source) to String (avatarUrl target).
    // The method name is flexible (e.g., getAvatarUrl, resolveAvatar) but the signature is key.
    public String mapAvatarUrl(User user) {
        if (user == null || user.getAttributes() == null) {
            return "https://res.cloudinary.com/dfcy1i11m/image/upload/v1763439718/avatars/avatar_1.jpg";
        }

        return user.getAttributes()
                .stream()
                .filter(attr -> "avatar".equals(attr.getKey()))
                .map(UserAttribute::getValue)
                .findFirst()
                .orElse("https://res.cloudinary.com/dfcy1i11m/image/upload/v1763439718/avatars/avatar_1.jpg");
    }
}