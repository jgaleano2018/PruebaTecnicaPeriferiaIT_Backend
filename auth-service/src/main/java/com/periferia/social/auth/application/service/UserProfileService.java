package com.periferia.social.auth.application.service;

import com.periferia.social.auth.domain.model.UserProfile;
import com.periferia.social.auth.domain.port.in.GetUserProfileUseCase;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.shared.exception.NotFoundException;
import java.util.UUID;

public class UserProfileService implements GetUserProfileUseCase {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfile getProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(UserProfile::of)
                .orElseThrow(() -> new NotFoundException("Usuario " + userId + " no encontrado"));
    }
}
