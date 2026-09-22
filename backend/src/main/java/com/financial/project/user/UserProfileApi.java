package com.financial.project.user;

import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserProfileApi {

    void syncFromJwt(Jwt jwt);

    Optional<ContactInfo> findContact(String customerId);
}
