package com.periferia.social.auth.domain.port.in;

import com.periferia.social.auth.domain.model.UserProfile;
import java.util.UUID;

public interface GetUserProfileUseCase {

    UserProfile getProfile(UUID userId);
}
