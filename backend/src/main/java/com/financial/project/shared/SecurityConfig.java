package com.financial.project.shared;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This is a stateless JWT resource server (Keycloak-issued bearer tokens, no
 * browser session/cookies), so CSRF protection - which guards against
 * ambient cookie-based credentials - does not apply and is disabled.
 * Without this, missing/invalid auth on state-changing requests returns 403
 * (CSRF) instead of the semantically correct 401 (unauthenticated).
 */
@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Settlement's SOAP endpoint simulates a call to a legacy core
                        // banking system; in production this would be an internal-only
                        // leg isolated by network/mTLS, not exposed for real - kept
                        // unauthenticated here to match the demo's scope.
                        .requestMatchers("/ws/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }
}
