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

## Durum

Şu an sadece dosya/klasör yapısı kuruldu; bağımlılıklar henüz indirilmedi, kod henüz yazılmadı.
