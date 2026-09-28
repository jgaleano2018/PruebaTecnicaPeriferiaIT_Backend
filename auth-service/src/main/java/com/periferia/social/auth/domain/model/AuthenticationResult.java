package com.periferia.social.auth.domain.model;

public record AuthenticationResult(AccessToken token, UserProfile user) {
}
