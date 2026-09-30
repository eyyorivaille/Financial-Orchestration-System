# Finansal İşlem ve Ödeme Orkestrasyon Platformu

Spring Boot tabanlı modular monolith bir ödeme orkestrasyon platformu simülasyonu — gerçek dünya bankacılık sistemlerine yakın mimari desenleri (event-driven orkestrasyon, double-entry ledger, SOAP legacy entegrasyonu, circuit breaker, OIDC) göstermek için yazılmış bir **CV/portfolyo projesi**. Canlıya alınması planlanmıyor; bkz. [Kapsam Felsefesi](#kapsam-felsefesi).

## Repo Yapısı

```
.
├── backend/    Spring Boot modular monolith (Java 21, Spring Modulith, MySQL, Kafka) — TAMAMLANDI
├── frontend/   React SPA (Vite + TypeScript) — henüz başlanmadı
└── docs/adr/   Mimari karar kayıtları (ADR)
```

## Backend Mimarisi

Akış: `İstek → Doğrulama (Keycloak JWT) → Risk Değerlendirme (senkron) → Sağlayıcı İşleme → Event Yayını (Kafka) → Bildirim / Denetim / Mutabakat (asenkron tüketiciler)`.

Her modül `com.financial.project.<modül>` altında, Spring Modulith'in `internal` alt-paket konvansiyonuyla yazıldı: kök paket o modülün **public API**'si (başka modüllerin senkron çağırabileceği tek yüzey), `internal` ise hiçbir modülün dışarıdan görmediği implementasyon detayı.

| Modül | Sorumluluk | Diğer modüllerle ilişkisi |
|---|---|---|
| `payment` | Ödeme state machine'i (`CREATED→PENDING→PROCESSING→COMPLETED/FAILED/CANCELLED`), idempotency (`Idempotency-Key` + unique constraint), sahte ödeme sağlayıcısı + circuit breaker | Risk'i senkron çağırır; `PaymentCreatedEvent`/`PaymentStatusChangedEvent`'i Kafka'ya yayınlar (Spring Modulith event externalization = transactional outbox) |
| `risk` | Konfigüre edilebilir kural motoru (tutar limiti, ülke kısıtı, velocity/frequency) → skor → APPROVE/REVIEW/REJECT | `RiskAssessmentApi` üzerinden Payment'a senkron cevap verir |
| `notification` | Ödeme tamamlanınca/başarısız olunca müşteriye bildirim (sahte email gönderici) | `payment-events`'i kendi Kafka consumer group'uyla dinler; alıcıyı `UserProfileApi`'den okur |
| `audit` | Her payment event'ini değiştirilemez bir denetim kaydına (JSON payload) düşürür | `payment-events`'i kendi consumer group'uyla dinler, filtre yok (hepsini kaydeder) |
| `settlement` | Tamamlanan ödemeleri sahte bir "legacy core banking" sistemine SOAP ile mutabakata gönderir, sonucu double-entry `ledger_entry` (debit/credit çifti) olarak defterler | `payment-events`'i kendi consumer group'uyla dinler; gerçek bir WSDL/XSD/JAXB SOAP round-trip'i (aynı app içindeki sahte endpoint'e) |
| `user` | Keycloak JWT claim'lerinden (email, ad-soyad) JIT (just-in-time) senkronize edilen müşteri profili | `UserProfileApi` ile Notification'a gerçek iletişim bilgisini sağlar |
| `shared` | Modüller arası paylaşılan altyapı: güvenlik zinciri, Kafka consumer factory/topic config, SOAP servlet config | — |

**Neden bazı modüller arası çağrı senkron (Modulith event/`ApplicationEventPublisher`), bazıları Kafka?** Payment↔Risk aynı transaction/JVM içinde kalması gereken bir karar zinciri — Kafka burada gereksiz gecikme/karmaşıklık olurdu. Notification/Audit/Settlement ise Payment'ın sonucunu *öğrenen*, ondan bağımsız çalışabilen tüketiciler — bu yüzden gevşek bağlı (Kafka). Ayrıntı: [docs/adr](docs/adr).

## Yerel Geliştirme

Altyapı (MySQL, Kafka, Keycloak) Docker Compose ile ayağa kalkar:

```
docker compose up -d
```

| Servis   | Bağlantı |
|----------|----------|
| MySQL    | `localhost:3307` (host'ta 3306 doluysa diye kaydırıldı), db `payment_platform`, user/pass `app`/`app` |
| Kafka    | `localhost:9092` |
| Keycloak | http://localhost:8081 (admin/admin) |

```
cd backend && ./mvnw spring-boot:run
```

Backend `localhost:8080`'de ayağa kalkar. API dokümantasyonu: http://localhost:8080/swagger-ui.html (springdoc-openapi).

### Keycloak (OIDC)

Realm ve client, Keycloak ilk açılışta `keycloak/realm-export.json` dosyasından otomatik import edilir.

| Alan | Değer |
|---|---|
| Realm | `payment-platform` |
| Frontend client | `payment-platform-frontend` (public, Authorization Code + PKCE) |
| Test kullanıcı (rol: `customer`) | `alice` / `alice123` |
| Test kullanıcı (rol: `ops`) | `bob` / `bob123` |
| Admin console | http://localhost:8081 (admin/admin) |

> `directAccessGrantsEnabled: true` sadece yerel test/curl ile token almayı kolaylaştırmak için — dev-only realm'de kabul edilebilir, gerçek bir production realm'de kapatılırdı.

### Örnek: bir ödeme oluşturma

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/realms/payment-platform/protocol/openid-connect/token \
  -d "grant_type=password&client_id=payment-platform-frontend&username=alice&password=alice123" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

curl -i -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-$(date +%s)" \
  -d '{"amountMinorUnits": 4500, "currency": "TRY", "countryCode": "TR"}'
```

Bu tek istek; risk değerlendirmesini, (sahte) sağlayıcı işlemesini, Kafka'ya event yayınını, bildirim gönderimini, denetim kaydını ve SOAP üzerinden mutabakat+ledger kaydını tetikler — hepsi asenkron tüketiciler üzerinden.

## Test

```bash
cd backend && ./mvnw test
```

Testcontainers (MySQL + Kafka) kullanır, yerel docker-compose'a bağımlı değildir. Her modül unit test (Mockito) + Testcontainers tabanlı bir entegrasyon testiyle (gerçek Kafka round-trip'i dahil) kapsanıyor.

## Kapsam Felsefesi

Bu proje bir CV/portfolyo simülasyonu — gelir elde etme, satış veya gerçek canlıya alma planı yok. Bu nedenle bazı kararlar bilinçli olarak sadeleştirildi (örn. veri retention job'ları, zorunlu PR review süreci, staging/prod ortam ayrımı), bazıları ise tam tersine bilinçli olarak *korundu* çünkü öğrenme/gösterme hedefinin kendisi (OAuth2/OIDC, Kafka + transactional outbox, double-entry ledger, SOAP, circuit breaker, idempotency). Gerekçeler [docs/adr](docs/adr) altında.

## Durum

**Backend tamamlandı**: 6 modül (payment, risk, notification, audit, settlement, user) uçtan uca çalışıyor — canlı doğrulanmış (docker-compose + gerçek Keycloak token'larıyla), otomatik test kapsamı (unit + Testcontainers entegrasyon) var. Sıradaki adım: frontend (React SPA).
