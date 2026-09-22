package com.financial.project.user.internal;

import com.financial.project.user.ContactInfo;
import com.financial.project.user.UserProfileApi;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
class UserProfileService implements UserProfileApi {

    private final UserProfileRepository userProfileRepository;

    UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void syncFromJwt(Jwt jwt) {
        String customerId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String displayName = displayName(jwt);

        UserProfile profile = userProfileRepository
                .findById(customerId)
                .map(existing -> {
                    existing.update(email, displayName);
                    return existing;
                })
                .orElseGet(() -> UserProfile.of(customerId, email, displayName));
        userProfileRepository.save(profile);
    }

    @Override
    public Optional<ContactInfo> findContact(String customerId) {
        return userProfileRepository
                .findById(customerId)
                .map(profile -> new ContactInfo(profile.getCustomerId(), profile.getEmail(), profile.getDisplayName()));
    }

    private String displayName(Jwt jwt) {
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        }
        return jwt.getClaimAsString("preferred_username");
    }
}
