# 0002 — Idempotency: DB unique constraint, distributed lock yok

## Context

Aynı ödeme isteği ağ hatası/timeout sonrası client tarafından tekrar gönderilebilir (retry). Aynı isteğin iki kez işlenip müşteriden iki kez para çekilmesi engellenmeli.

## Decision

Client her istekte bir `Idempotency-Key` header'ı gönderir. `payment_transaction.idempotency_key` kolonu `unique` constraint'li; `PaymentService.createPayment` önce bu key ile var olan kaydı arar, varsa onu döner (yeni işlem başlatmaz). Redis/Zookeeper tabanlı bir distributed lock kullanılmadı.

## Consequences

- Tek instance'lık bir demo için bu yeterli; DB'nin unique constraint'i zaten race condition'ı (iki eşzamanlı istek) DB seviyesinde güvenli şekilde çözer (ikinci `insert` constraint violation'la başarısız olur, kod bunu `findByIdempotencyKey`'e düşerek ele alır).
- Çoklu instance/gerçek ölçekte, constraint aynı garantiyi verir — asıl atlanan şey sadece *lock'u DB dışında erken alıp gereksiz çalışmayı önlemek* optimizasyonu, doğruluk garantisi değil.
