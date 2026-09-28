package com.periferia.social.shared.security;

/** Claims personalizados que emite auth-service y consumen los demás servicios. */
public final class JwtClaimNames {

    public static final String USERNAME = "preferred_username";
    public static final String DISPLAY_NAME = "name";
    public static final String ROLES = "roles";

    private JwtClaimNames() {
    }
}
