package com.financial.project.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financial.project.user.ContactInfo;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(userProfileRepository);
    }

    @Test
    void syncFromJwtCreatesANewProfileWhenNoneExists() {
        when(userProfileRepository.findById("customer-1")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.syncFromJwt(jwt("customer-1", "alice@example.com", "Alice", "Customer", "alice"));

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo("customer-1");
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(captor.getValue().getDisplayName()).isEqualTo("Alice Customer");
    }

    @Test
    void syncFromJwtFallsBackToPreferredUsernameWhenNameClaimsAreMissing() {
        when(userProfileRepository.findById("customer-1")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.syncFromJwt(jwt("customer-1", "alice@example.com", null, null, "alice"));

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getDisplayName()).isEqualTo("alice");
    }

    @Test
    void syncFromJwtUpdatesAnExistingProfile() {
        UserProfile existing = UserProfile.of("customer-1", "old@example.com", "Old Name");
        when(userProfileRepository.findById("customer-1")).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.syncFromJwt(jwt("customer-1", "new@example.com", "New", "Name", "customer-1"));

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().getEmail()).isEqualTo("new@example.com");
        assertThat(captor.getValue().getDisplayName()).isEqualTo("New Name");
    }

    @Test
    void findContactReturnsEmptyWhenNoProfileSynced() {
        when(userProfileRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<ContactInfo> contact = userProfileService.findContact("unknown");

        assertThat(contact).isEmpty();
    }

    @Test
    void findContactReturnsSyncedProfile() {
        when(userProfileRepository.findById("customer-1"))
                .thenReturn(Optional.of(UserProfile.of("customer-1", "alice@example.com", "Alice Customer")));

        Optional<ContactInfo> contact = userProfileService.findContact("customer-1");

        assertThat(contact).contains(new ContactInfo("customer-1", "alice@example.com", "Alice Customer"));
    }

    private Jwt jwt(String subject, String email, String givenName, String familyName, String preferredUsername) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("preferred_username", preferredUsername);
        if (email != null) {
            builder.claim("email", email);
        }
        if (givenName != null) {
            builder.claim("given_name", givenName);
        }
        if (familyName != null) {
            builder.claim("family_name", familyName);
        }
        return builder.build();
    }
}
