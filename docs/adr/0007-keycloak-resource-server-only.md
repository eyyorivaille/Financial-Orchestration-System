# 0007 — Keycloak: sadece OAuth2 Resource Server, kendi Authorization Server'ımız yok

## Context

Kimlik doğrulama/parola yönetimi/token issuance kendi başına büyük bir problem alanı (OWASP'ın en çok hata yapılan konularından biri). Bunu sıfırdan yazmak, bu projenin öğrenme hedefine (ödeme orkestrasyonu) hiçbir katkısı olmayan bir yük.

## Decision

Keycloak, Docker ile hazır bir OIDC provider olarak kullanılıyor (realm/client `keycloak/realm-export.json`'dan otomatik import). Spring uygulaması sadece `spring-boot-starter-oauth2-resource-server` ile JWT doğrulayan bir Resource Server; kendi Authorization Server'ımız yok.

## Consequences

- Gerçek bir OIDC Authorization Code + PKCE akışı, gerçek bir JWT doğrulama zinciri deneyimlendi — sahte/basitleştirilmiş bir auth değil.
- Backend'de rol bazlı yetkilendirme (`customer` vs `ops`) `@PreAuthorize`/`JwtAuthenticationConverter` ile tam bir GrantedAuthority setup'ına bağlanmadı; sadece ihtiyaç olan noktada (`PaymentController.getPayment`, ownership kontrolü) `realm_access.roles` claim'i doğrudan okunuyor. Daha kapsamlı bir yetkilendirme modeli gerekirse bu ADR'ın kapsamını genişletmesi gerekir.
- `directAccessGrantsEnabled: true` sadece yerel curl/test kolaylığı için — gerçek bir production realm'de kapatılırdı (README'de not edildi).
