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

Backend, bu servislere `backend/src/main/resources/application.properties` üzerinden bağlanacak şekilde önceden yapılandırıldı. Keycloak realm/client henüz oluşturulmadı — OAuth2 Resource Server ayarı (`issuer-uri`) o adıma kadar yorum satırında bekliyor.

```
cd backend && ./mvnw spring-boot:run
```

## Durum

Dosya/klasör yapısı, bağımlılıklar ve yerel Docker Compose altyapısı hazır; backend Flyway migration'ıyla (Spring Modulith event publication tablosu) uçtan uca doğrulandı. Keycloak realm/client kurulumu ve gerçek domain kodu (entity/controller/business logic) henüz yazılmadı.
