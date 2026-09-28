package com.periferia.social.auth.infrastructure.adapter.in.web.dto;

import com.periferia.social.auth.domain.model.UserProfile;
import java.util.UUID;

public record UserResponse(UUID id, String username, String displayName) {

    public static UserResponse from(UserProfile profile) {
        return new UserResponse(profile.id(), profile.username(), profile.displayName());
    }
}
