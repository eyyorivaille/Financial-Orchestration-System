# Finansal İşlem ve Ödeme Orkestrasyon Platformu

Spring Boot tabanlı modular monolith bir ödeme orkestrasyon platformu simülasyonu (CV/portfolyo projesi — canlıya alınması planlanmıyor).

## Repo Yapısı

```
.
├── backend/    Spring Boot modular monolith (Java 21, Spring Modulith, MySQL, Kafka)
├── frontend/   React SPA (Vite + TypeScript)
└── docs/adr/   Mimari karar kayıtları
```

## Backend Modülleri (Spring Modulith)

`backend/src/main/java/com/financial/project/`:

- `payment` — ödeme state machine'i, idempotency, transactional outbox
- `risk` — kural bazlı fraud/risk skorlama
- `notification` — bildirim gönderimi
- `audit` — append-only audit log
- `settlement` — legacy core banking sistemine SOAP entegrasyonu
- `user` — kullanıcı/hesap yönetimi
- `shared` — modüller arası paylaşılan ortak kod (shared kernel)

## Yerel Geliştirme

Altyapı (MySQL, Kafka, Keycloak) Docker Compose ile ayağa kalkar:

```
docker compose up -d
```

| Servis   | Bağlantı                                  |
|----------|--------------------------------------------|
| MySQL    | `localhost:3307` (host'ta 3306 doluysa diye kaydırıldı), db `payment_platform`, user/pass `app`/`app` |
| Kafka    | `localhost:9092`                            |
| Keycloak | http://localhost:8081 (admin/admin)         |

Backend, bu servislere `backend/src/main/resources/application.properties` üzerinden bağlanacak şekilde yapılandırıldı.

```
cd backend && ./mvnw spring-boot:run
```

### Keycloak (OIDC)

Realm ve client, Keycloak ilk açılışta `keycloak/realm-export.json` dosyasından otomatik import edilir (elle admin console'dan kurulum gerekmez).

| Alan | Değer |
|---|---|
| Realm | `payment-platform` |
| Frontend client | `payment-platform-frontend` (public, Authorization Code + PKCE) |
| Redirect URI | `http://localhost:5173/*` (Vite dev server) |
| Test kullanıcı (rol: `customer`) | `alice` / `alice123` |
| Test kullanıcı (rol: `ops`) | `bob` / `bob123` |
| Admin console | http://localhost:8081 (admin/admin) |

> `directAccessGrantsEnabled: true` sadece yerel test/curl ile token almayı kolaylaştırmak için — dev-only realm'de kabul edilebilir, gerçek bir production realm'de kapatılırdı.

Doğrulama: `GET /v3/api-docs` token'sız `401`, geçerli bir Keycloak JWT'siyle `200` döner.

## Durum

Dosya/klasör yapısı, bağımlılıklar, yerel Docker Compose altyapısı (MySQL, Kafka, Keycloak) ve Keycloak realm/client kurulumu hazır; backend OAuth2 Resource Server olarak uçtan uca doğrulandı (Flyway migration, JWT doğrulama). Gerçek domain kodu (entity/controller/business logic) henüz yazılmadı.
