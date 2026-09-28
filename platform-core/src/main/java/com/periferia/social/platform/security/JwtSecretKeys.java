package com.periferia.social.platform.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** Deriva la {@link SecretKey} HS256 a partir del secreto configurado. */
public final class JwtSecretKeys {

    public static final String ALGORITHM = "HmacSHA256";

    private JwtSecretKeys() {
    }

    public static SecretKey hmacKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }
}
