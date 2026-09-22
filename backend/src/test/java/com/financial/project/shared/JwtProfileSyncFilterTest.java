package com.financial.project.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.financial.project.user.UserProfileApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class JwtProfileSyncFilterTest {

    @Mock
    private UserProfileApi userProfileApi;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtProfileSyncFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtProfileSyncFilter(userProfileApi);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void syncsProfileWhenAuthenticatedWithAJwt() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("customer-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        filter.doFilter(request, response, filterChain);

        verify(userProfileApi).syncFromJwt(jwt);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNothingWhenAuthenticationIsNotAJwt() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("someone", "password"));

        filter.doFilter(request, response, filterChain);

        verify(userProfileApi, never()).syncFromJwt(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNothingWhenUnauthenticated() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(userProfileApi, never()).syncFromJwt(any());
        verify(filterChain).doFilter(request, response);
    }
}
