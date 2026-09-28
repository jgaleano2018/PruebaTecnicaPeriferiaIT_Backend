package com.periferia.social.auth.domain.port.out;

import com.periferia.social.auth.domain.model.AccessToken;
import com.periferia.social.auth.domain.model.User;

public interface AccessTokenIssuer {

    AccessToken issueFor(User user);
}
