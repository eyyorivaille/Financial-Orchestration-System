# 0008 — Kullanıcı profili: Keycloak Admin API yerine JWT'den JIT senkron

## Context

Notification'ın müşteriye gerçek bir email adresine bildirim göndermesi gerekiyor, ama bu bilgi Payment'ın event'lerinde yok (sadece `customerId`/`sub` var). Bu bilgiyi almanın bir yolu Keycloak Admin REST API'sini çağırmak — ama bu, bir servis hesabı, client credentials, rol ataması gibi ek bir kimlik/altyapı kurulumu gerektirir.

## Decision

Keycloak'ın zaten JWT'ye gömdüğü claim'ler (`email`, `given_name`, `family_name`, `preferred_username`) kullanılarak **just-in-time (JIT) provisioning** yapıldı: her authenticated request'te `shared.JwtProfileSyncFilter`, `user.UserProfileApi.syncFromJwt(jwt)` üzerinden local `user_profile` satırını upsert eder. Notification, gerçek email'i `UserProfileApi.findContact` ile buradan okur (profil hiç senkronize olmadıysa eski placeholder'a düşer).

## Consequences

- Keycloak Admin API'sine, servis hesabına veya ek bir role/credential kurulumuna gerek kalmadı — JWT'nin zaten taşıdığı bilgi yeniden kullanıldı.
- Bu, gerçek dünyada da yaygın kullanılan, meşru bir desen (JIT provisioning) — bir "kestirme" değil.
- Bedel: Profil sadece kullanıcı en az bir authenticated istek yaptıktan SONRA var olur. Hiç giriş yapmamış bir kullanıcı için `findContact` boş döner (Notification bunu placeholder'a düşerek ele alıyor).
