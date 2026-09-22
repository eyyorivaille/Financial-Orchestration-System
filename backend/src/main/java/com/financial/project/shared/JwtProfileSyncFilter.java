package com.financial.project.shared;

import com.financial.project.user.UserProfileApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Just-in-time provisioning: syncs the local user_profile row from the bearer
 * token's claims on every authenticated request, so modules that need real
 * contact info (e.g. notification) don't have to call Keycloak themselves.
 */
@Component
class JwtProfileSyncFilter extends OncePerRequestFilter {

    private final UserProfileApi userProfileApi;

    JwtProfileSyncFilter(UserProfileApi userProfileApi) {
        this.userProfileApi = userProfileApi;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            userProfileApi.syncFromJwt(jwtAuthentication.getToken());
        }
        filterChain.doFilter(request, response);
    }
}
